# SECURITY_SYSTEM

Baseado em [[System/CarmarqControl - Briefing]], [[System/CarmarqControl - Visão Geral]], [[Rules/AI - Regras]] e [[AI/Agents/security]].

## Escopo e evidências

- Backend analisado em `..\src\main\java\com\example\...`
- Frontend analisado em `..\carmarq-control-frontend\src\...`
- Configuração analisada em `..\src\main\resources\application.properties`
- Testes analisados em `..\src\test\java\...`
- `SPEC_<FEATURE>.md`: `Informação não encontrada no sistema`
- Migrations/DDL detalhado do PostgreSQL: `Informação não encontrada no sistema`

## Resumo de segurança

O sistema está em **risco alto**.

Os principais motivos são:

- autenticação baseada em cookie com `CSRF` desabilitado;
- credenciais padrão previsíveis e segredo JWT com fallback conhecido;
- falhas de autorização com impacto financeiro e operacional;
- um `IDOR` confirmado na exclusão de fotos;
- vazamento de detalhes internos por erros `500`;
- ausência de testes automatizados de segurança/autorização.

## Riscos identificados

### 1. CSRF desabilitado com autenticação por cookie

- **Criticidade:** alto
- **Evidência:**
  - `..\src\main\java\com\example\Config\SecurityConfig.java:31`
  - `..\src\main\java\com\example\Config\SecurityConfig.java:62-72`
  - `..\carmarq-control-frontend\src\main.jsx:8-11`
- **Problema:**
  - o sistema autentica por cookie (`accessToken`) e permite `credentials`, mas desabilita `CSRF`;
  - o frontend configura `xsrfCookieName` e `xsrfHeaderName`, porém o backend não gera nem valida token CSRF.
- **Exemplo de ataque:**
  - uma origem permitida, uma aplicação local em `localhost`, um subdomínio comprometido ou um XSS em origem confiável pode disparar requisições autenticadas em nome do usuário logado.
- **Correção prática:**
  - habilitar `CookieCsrfTokenRepository.withHttpOnlyFalse()` ou estratégia equivalente;
  - validar `X-XSRF-TOKEN` em todos os métodos mutáveis;
  - revisar `allowedOrigins` por ambiente e evitar configuração de desenvolvimento em produção.

### 2. Credenciais padrão previsíveis e segredos sensíveis hardcoded

- **Criticidade:** alto
- **Evidência:**
  - `..\src\main\java\com\example\Config\DataInitializer.java:19-46`
  - `..\src\main\resources\application.properties:5-15`
- **Problema:**
  - o bootstrap cria contas com credenciais conhecidas:
    - `admin@carmaq.com / admin123`
    - `proprietario@empresa.com / 123123`
  - o arquivo de configuração contém:
    - usuário/senha do PostgreSQL em texto puro;
    - `jwt.secret` com fallback previsível.
- **Exemplo de ataque:**
  - em ambiente novo ou restaurado, basta tentar as credenciais padrão para obter acesso de `PROPRIETARIO`.
- **Correção prática:**
  - remover usuários padrão fora de ambiente local controlado;
  - mover segredos para variáveis de ambiente/secret manager;
  - rotacionar imediatamente senhas e segredo JWT;
  - impedir startup com segredo default.

### 3. JWT via cookie sem `Secure`, sem `SameSite`, sem revogação e sem checagem de estado da conta

- **Criticidade:** alto
- **Evidência:**
  - `..\src\main\java\com\example\Service\JwtService.java:79-97`
  - `..\src\main\java\com\example\Config\JwtAuthenticationFilter.java:48-63`
  - `..\src\main\java\com\example\Service\JwtService.java:59-68`
  - `..\src\main\java\com\example\Controller\AuthController.java:72-82`
  - `..\src\main\resources\application.properties:13-15`
- **Problema:**
  - cookie com `HttpOnly`, mas `Secure=false`;
  - `SameSite`: `Informação não encontrada no sistema`;
  - logout apenas remove cookie do navegador, sem revogação server-side;
  - o filtro aceita token válido por assinatura/expiração, mas não invalida sessão de usuário desativado ou bloqueado.
- **Exemplo de ataque:**
  - um cookie roubado continua utilizável até expirar, mesmo após desativação da conta;
  - em ambiente HTTP, o cookie pode ser interceptado.
- **Correção prática:**
  - usar `Secure=true` em produção;
  - definir `SameSite=Lax` ou `Strict` conforme fluxo real;
  - reduzir TTL do access token e usar refresh token com rotação;
  - adotar `tokenVersion`, blacklist ou tabela de sessão;
  - no filtro, validar `isEnabled`, bloqueio e versão da sessão.

### 4. Falha de autorização em transição de status da OS

- **Criticidade:** alto
- **Evidência:**
  - `..\src\main\java\com\example\Controller\ServiceOrderController.java:97-102`
  - `..\src\main\java\com\example\Service\ServiceOrderService.java:324-360`
  - `..\carmarq-control-frontend\src\Pages\OrdemDetalhes.jsx:427-435`
- **Problema:**
  - `TECNICO` pode chamar `PUT /api/service-orders/{id}/status`;
  - o backend restringe `PROPRIETARIO` e `FINANCEIRO`, mas não restringe os status que o técnico pode enviar;
  - a própria UI mostra botão de cancelamento para qualquer usuário não financeiro.
- **Exemplo de ataque:**
  - um técnico pode enviar `CANCELADA`, `PAGO` ou outros estados indevidos em ordens atribuídas a ele.
- **Correção prática:**
  - no backend, whitelistar explicitamente os status permitidos por role;
  - remover qualquer decisão de segurança da UI;
  - retornar `403` para combinações role/status proibidas.

### 5. Técnico pode alterar `reimbursementValue` da própria OS

- **Criticidade:** alto
- **Evidência:**
  - `..\src\main\java\com\example\Controller\ServiceOrderController.java:146-151`
  - `..\src\main\java\com\example\Service\ServiceOrderService.java:623-641`
  - `..\carmarq-control-frontend\src\Pages\OrdemDetalhes.jsx:716-741`
- **Problema:**
  - `TECNICO` pode lançar reembolso extra diretamente;
  - não existe limite, faixa máxima, exigência de comprovante ou fluxo de aprovação antes do valor entrar no cálculo financeiro.
- **Exemplo de ataque:**
  - o técnico define reembolso arbitrariamente alto e ele passa a compor o repasse final.
- **Correção prática:**
  - restringir alteração de reembolso a `PROPRIETARIO`/`FINANCEIRO`, ou;
  - manter o técnico como solicitante e criar status de aprovação separado;
  - validar teto, motivo e comprovante.

### 6. IDOR confirmado na exclusão de fotos

- **Criticidade:** alto
- **Evidência:**
  - `..\src\main\java\com\example\Controller\ServicePhotoController.java:75-80`
  - `..\src\main\java\com\example\Service\ServicePhotoService.java:88-104`
- **Problema:**
  - `deletePhoto(photoId)` não valida ownership;
  - o `serviceOrderId` da rota não é usado para autorizar a exclusão;
  - basta conhecer `photoId`.
- **Exemplo de ataque:**
  - um técnico autenticado pode apagar foto de outra OS enviando `DELETE /api/service-orders/qualquer-id/photos/{photoId}`.
- **Correção prática:**
  - validar no serviço se a foto pertence à OS da rota;
  - validar se o técnico é o responsável pela OS da foto;
  - idealmente buscar por `(photoId, serviceOrderId)` e negar se não houver match.

### 7. Vazamento de dados financeiros para técnico via API

- **Criticidade:** médio
- **Evidência:**
  - `..\src\main\java\com\example\Service\ServiceOrderService.java:496-503`
  - `..\carmarq-control-frontend\src\Pages\Ordens.jsx:177-184`
  - `..\carmarq-control-frontend\src\Pages\OrdemDetalhes.jsx:611-675`
- **Problema:**
  - a UI esconde parte dos valores, mas a API de detalhe da OS entrega ao técnico:
    - `serviceValue`
    - `travelValue`
    - `partsValue`
    - `expensesValue`
    - `displacementValue`
    - `reimbursementValue`
- **Exemplo de ataque:**
  - o técnico consulta o JSON no navegador ou por script e obtém composição financeira que a interface não exibe explicitamente.
- **Correção prática:**
  - criar DTOs diferentes por papel;
  - remover do payload do técnico tudo que não for estritamente necessário;
  - revisar exposição também em dashboard/exportações.

### 8. Respostas de erro inadequadas e vazamento de detalhes internos

- **Criticidade:** médio
- **Evidência:**
  - `..\src\main\java\com\example\Config\GlobalExceptionHandler.java:53-62`
  - `..\src\main\java\com\example\Service\ServiceOrderService.java:81-84`
  - `..\src\main\java\com\example\Service\ServiceOrderService.java:312-313`
  - `..\src\main\java\com\example\Service\ServiceOrderService.java:332-333`
- **Problema:**
  - exceções genéricas retornam `500` com `e.getMessage()`;
  - diversas negações de acesso usam `RuntimeException` em vez de `AccessDeniedException`;
  - isso mistura erro de autorização com erro interno e vaza mensagens internas.
- **Exemplo de ataque:**
  - um atacante força caminhos proibidos e usa a diferença entre `403`, `404` e `500` para mapear comportamento interno.
- **Correção prática:**
  - padronizar `403` para acesso negado;
  - nunca devolver mensagem interna crua ao cliente;
  - registrar stack trace apenas em logs estruturados protegidos.

### 9. Validação de entrada incompleta em login e gestão de usuários

- **Criticidade:** médio
- **Evidência:**
  - `..\src\main\java\com\example\DTOs\LoginRequestDTO.java:5-8`
  - `..\src\main\java\com\example\DTOs\UserRegistrationDTO.java:5-11`
  - `..\src\main\java\com\example\DTOs\UserUpdateDTO.java:5-12`
  - `..\src\main\java\com\example\Controller\AuthController.java:30-31`
  - `..\src\main\java\com\example\Controller\UserController.java:39-54`
- **Problema:**
  - ausência de `@Valid`;
  - DTOs sem `@NotBlank`, `@Email`, `@Size`, enum de role ou política de senha;
  - payloads nulos/malformados podem gerar `500`, inconsistência de dados e respostas fracas.
- **Exemplo de ataque:**
  - envio de `role=null`, `senha=null` ou payload inválido para provocar comportamento inesperado e respostas internas.
- **Correção prática:**
  - adicionar validação bean validation em todos os DTOs de autenticação e usuário;
  - restringir `role` a enum;
  - aplicar política mínima de senha e normalização de email.

### 10. Técnicos conseguem enumerar todos os clientes e todas as máquinas ativas

- **Criticidade:** médio
- **Evidência:**
  - `..\src\main\java\com\example\Controller\ClientController.java:23-25`
  - `..\src\main\java\com\example\Controller\MachineController.java:23-25`
  - `..\src\main\java\com\example\Service\ClientService.java:31-41`
  - `..\src\main\java\com\example\Service\MachineService.java:30-40`
  - repositórios com alternativas não utilizadas:
    - `..\src\main\java\com\example\Repository\ClientRepository.java:18`
    - `..\src\main\java\com\example\Repository\MachineRepository.java:21`
- **Problema:**
  - o técnico recebe catálogo completo de clientes/máquinas ativos, inclusive contato e geolocalização do cliente.
- **Exemplo de ataque:**
  - um técnico exporta manualmente base de clientes, endereços e coordenadas sem necessidade operacional imediata.
- **Correção prática:**
  - aplicar escopo mínimo por técnico;
  - expor catálogo completo apenas para papéis administrativos;
  - se o catálogo completo for realmente requisito, registrar isso formalmente.

### 11. Upload de foto com validação fraca

- **Criticidade:** médio
- **Evidência:**
  - `..\src\main\java\com\example\Service\ServicePhotoService.java:51-53`
  - `..\src\main\java\com\example\Service\ServicePhotoService.java:63-76`
  - `..\src\main\java\com\example\Controller\ServicePhotoController.java:70-72`
- **Problema:**
  - a validação aceita somente `contentType` declarado pelo cliente;
  - não há inspeção do conteúdo real do arquivo;
  - a resposta sempre envia `image/jpeg`, mesmo quando o arquivo pode ser PNG/GIF/WebP.
- **Exemplo de ataque:**
  - envio de arquivo poliglota ou conteúdo não esperado com MIME forjado.
- **Correção prática:**
  - validar magic number/assinatura do arquivo;
  - limitar extensões e tipos reais;
  - devolver `Content-Type` real e `X-Content-Type-Options: nosniff`.

### 12. Rate limiting pode ser burlado por `X-Forwarded-For`

- **Criticidade:** médio
- **Evidência:**
  - `..\src\main\java\com\example\Config\RateLimitingFilter.java:71-83`
- **Problema:**
  - o filtro aceita `X-Forwarded-For` sem proxy confiável;
  - um cliente pode trocar o header e ganhar buckets novos.
- **Exemplo de ataque:**
  - brute force distribuído artificialmente usando IPs falsos no header.
- **Correção prática:**
  - confiar em `X-Forwarded-For` apenas atrás de proxy validado;
  - caso contrário, usar `getRemoteAddr()`;
  - considerar rate limit por usuário e por rota sensível.

### 13. Ausência de testes de autenticação/autorização

- **Criticidade:** baixo
- **Evidência:**
  - `..\src\test\java\...` contém apenas testes de contexto e cálculo;
  - `Informação não encontrada no sistema` sobre testes automatizados de CSRF, JWT, roles e IDOR.
- **Problema:**
  - regressões de segurança podem entrar sem cobertura.
- **Correção prática:**
  - criar testes de:
    - login/logout;
    - acesso por `Owner`, `Technician` e `Financial`;
    - negativa de acesso;
    - IDOR;
    - transições de status permitidas/proibidas.

## Análise por tema

### Autenticação

- **Pontos positivos**
  - senha armazenada com `BCrypt`;
  - `HttpOnly` presente no cookie;
  - lock de conta por tentativas falhas existe no login.
- **Pontos críticos**
  - segredo JWT previsível por fallback;
  - cookie sem `Secure` e sem `SameSite` explícito;
  - sem revogação/rotação;
  - token continua válido mesmo com conta desativada ou bloqueada.

### Autorização

- **Pontos positivos**
  - uso extensivo de `@PreAuthorize`;
  - vários fluxos de OS têm checagem extra de ownership.
- **Pontos críticos**
  - `ServicePhotoService.deletePhoto` confirma um `IDOR`;
  - `updateStatus` permite transições indevidas a técnico;
  - `updateReimbursement` permite impacto financeiro direto por técnico;
  - acesso de técnico a clientes/máquinas é amplo.

### Validação de entrada

- **Com validação adequada**
  - clientes, máquinas, peças, despesas, OS e apontamentos de tempo possuem parte da validação por DTO.
- **Com lacunas**
  - login;
  - criação/edição de usuário;
  - valores financeiros sensíveis sem faixa mínima/máxima no backend;
  - `serviceType`, `manutencaoOrigin` e `role` sem enum forte em todas as entradas.

## Vulnerabilidades solicitadas

### SQL Injection

- **Conclusão:** não encontrei evidência direta de SQL Injection no código analisado.
- **Base:**
  - repositórios usam Spring Data JPA/JPQL com parâmetros (`@Param`, `findBy...`, `@Query` sem concatenação de entrada em SQL bruto).

### XSS

- **Conclusão:** não encontrei sink XSS direto no frontend analisado.
- **Base:**
  - `dangerouslySetInnerHTML` e `innerHTML`: `Informação não encontrada no sistema`
  - React escapa conteúdo por padrão.
- **Risco residual:**
  - o backend não sanitiza texto persistido;
  - se outra camada futura renderizar HTML cru, o risco reaparece.

### CSRF

- **Conclusão:** vulnerabilidade relevante encontrada.
- **Base:**
  - autenticação por cookie + `withCredentials` + CSRF desligado.

### Broken Authentication

- **Conclusão:** vulnerabilidade relevante encontrada.
- **Base:**
  - credenciais padrão;
  - segredo JWT previsível;
  - sessão não revogada;
  - conta desativada continua com token válido.

### IDOR

- **Conclusão:** vulnerabilidade confirmada.
- **Base:**
  - exclusão de foto por `photoId` sem validação de ownership no serviço.

## API Security

- **Endpoints expostos relevantes**
  - `/api/auth/login`
  - `/api/auth/validate`
  - `/api/auth/logout`
  - `/api/service-orders/**`
  - `/api/users/**`
  - `/api/clients/**`
  - `/api/machines/**`
- **Vazamento de dados**
  - valores financeiros internos expostos a técnico na API de OS;
  - mensagens internas devolvidas em `500`;
  - `spring.jpa.show-sql=true` em configuração local.
- **Status codes impróprios**
  - várias negativas de acesso viram `500` por uso de `RuntimeException`;
  - alguns controladores retornam `400`/`404` genéricos sem payload consistente.

## Cenários reais de ataque

### Cenário 1. Login administrativo com credencial padrão

1. O atacante acessa a tela de login.
2. Tenta `admin@carmaq.com / admin123`.
3. Se o ambiente foi iniciado com a base vazia, ganha acesso de `PROPRIETARIO`.

### Cenário 2. Técnico cancela uma OS sem autorização de negócio

1. O técnico abre uma OS atribuída a ele.
2. Envia `PUT /api/service-orders/{id}/status` com `{ "status": "CANCELADA" }`.
3. O backend aceita a transição.
4. Resultado: impacto operacional indevido e potencial perda financeira.

### Cenário 3. Técnico apaga foto de outra OS

1. O técnico descobre ou enumera um `photoId`.
2. Envia `DELETE /api/service-orders/1/photos/{photoId}`.
3. O serviço apaga a foto sem conferir se ela pertence à OS dele.
4. Resultado: perda de evidência operacional.

### Cenário 4. Cookie roubado permanece útil após desativação da conta

1. O atacante obtém o cookie `accessToken`.
2. O administrador desativa o usuário.
3. O atacante continua chamando a API até o JWT expirar.
4. Resultado: sessão efetivamente não revogada.

## Melhorias específicas solicitadas

### Spring Security

- habilitar CSRF para autenticação por cookie;
- usar `CookieCsrfTokenRepository`;
- criar `AuthenticationEntryPoint` e `AccessDeniedHandler` padronizados;
- centralizar `PasswordEncoder` como bean;
- separar regras de dev e prod para CORS;
- adicionar `X-Content-Type-Options`, `Referrer-Policy` e `Permissions-Policy`;
- remover `xssProtection` legado e depender de CSP moderna + escaping seguro.

### JWT via cookies

- `Secure=true` em produção;
- `HttpOnly=true` mantido;
- definir `SameSite=Lax` ou `Strict`;
- access token curto;
- refresh token rotativo;
- revogação por versão/sessão;
- checar usuário ativo/bloqueado a cada request.

### React security practices

- manter token fora de `localStorage` para autenticação;
- não confiar em `PrivateRoute` como controle de segurança;
- validar inputs também no cliente para reduzir abuso e erro operacional;
- centralizar `axios` com interceptors para `401/403`;
- evitar exibir ações que o backend não autoriza;
- continuar sem `dangerouslySetInnerHTML`.

## Boas práticas adicionais

- criar auditoria de ações sensíveis:
  - login;
  - mudança de status;
  - aprovação/rejeição de pagamento;
  - alteração de reembolso;
  - exclusão de foto;
- adicionar trilha de aprovação para despesas e reembolsos;
- revisar segregação real entre `Owner`, `Technician` e `Financial`;
- remover logs sensíveis de console;
- testar cenários negativos em CI.

## Itens sem evidência suficiente

- política formal de rotação de segredos: `Informação não encontrada no sistema`
- ambiente de produção, domínio final e HTTPS obrigatório: `Informação não encontrada no sistema`
- WAF, proxy reverso e trusted proxies: `Informação não encontrada no sistema`
- esquema SQL/migrations detalhados do PostgreSQL: `Informação não encontrada no sistema`
