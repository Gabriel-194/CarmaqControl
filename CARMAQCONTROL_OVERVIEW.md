# Visão Geral do Sistema CarmaqControl

## Resumo Executivo
O CarmaqControl é um sistema de gestão de ordens de serviço para atendimento técnico de máquinas industriais, com controle operacional (execução de OS, tempos, peças, despesas e fotos), controle financeiro (faturamento, repasses, lucro estimado) e painéis por perfil (`PROPRIETARIO`, `FINANCEIRO`, `TECNICO`).

Evidências principais:
- `src/main/java/com/example/Service/ServiceOrderService.java:96-167`, `324-370`, `433-543`
- `src/main/java/com/example/Service/DashboardService.java:35-224`
- `carmarq-control-frontend/src/Pages/Ordens.jsx:73-257`
- `carmarq-control-frontend/src/Pages/OrdemDetalhes.jsx:217-777`

## Tecnologias Identificadas
### Backend
- Java 17 + Spring Boot (`pom.xml:17`, `1-15`)
- Spring MVC, Spring Security, Spring Data JPA (`pom.xml:21-37`)
- PostgreSQL (`pom.xml:44-48`, `src/main/resources/application.properties:5-8`)
- JWT com `jjwt` (`pom.xml:61-84`, `src/main/java/com/example/Service/JwtService.java:51-103`)
- Rate limiting com Bucket4j (`pom.xml:85-89`, `src/main/java/com/example/Config/RateLimitingFilter.java:18-69`)
- Relatórios Excel com Apache POI e componentes iText no projeto (`pom.xml:90-112`; geração em `ReportService.java`)

### Frontend
- React 19 + Vite (`carmarq-control-frontend/package.json:12-16`, `25-30`)
- React Router (`carmarq-control-frontend/src/main.jsx:3`, `30-64`)
- Axios para integração com backend (`carmarq-control-frontend/src/main.jsx:6-10`)
- Lucide React para ícones (`carmarq-control-frontend/package.json:13`)

### Integrações externas identificadas
- CNPJ: `publica.cnpj.ws` e fallback `brasilapi.com.br`
- CEP/endereço: `viacep.com.br`
- Geocodificação: `nominatim.openstreetmap.org`

Evidências:
- `carmarq-control-frontend/src/Components/Clients/ClientModal.jsx:67-68`, `93-94`, `117-127`

## Estrutura do Projeto
### Raiz
- Backend Spring Boot em `src/`
- Frontend React/Vite em `carmarq-control-frontend/`
- Documentação funcional em `PRD_FUNCIONALIDADES.md` e `SPEC_FUNCIONALIDADES.md`

### Backend (`src/main/java/com/example`)
- `Config/`: segurança JWT, CORS/CSRF, rate limit, bootstrap de usuários
- `Controller/`: APIs REST por módulo
- `Service/`: regras de negócio e orquestração
- `Repository/`: acesso a dados (JPA + consultas customizadas)
- `Models/`: entidades JPA
- `DTOs/`: contratos de entrada/saída
- `Domain/`: enums de domínio

### Frontend (`carmarq-control-frontend/src`)
- `Pages/`: telas principais (`Login`, `Dashboard`, `Ordens`, `OrdemDetalhes`, `NovaOS`, `Clients`, `Machines`, `Usuarios`)
- `Components/`: blocos reutilizáveis (tempos, peças, despesas, fotos, dashboards, sidebar)
- `contexts/`: autenticação e tema
- `utils/`: máscaras e mapeamentos de status/tipos
- `Constants/`: metadados de máquina

Evidências:
- `src/main/java/com/example/*`
- `carmarq-control-frontend/src/*`

## Módulos Principais
### 1) Autenticação e sessão
- Login/logout/validate por cookie JWT (`/api/auth/*`)
- Controle de acesso por role no backend (`@PreAuthorize`) e no frontend (`PrivateRoute`)

Evidências:
- `src/main/java/com/example/Controller/AuthController.java:22-99`
- `src/main/java/com/example/Config/SecurityConfig.java:29-47`
- `carmarq-control-frontend/src/contexts/AuthContext.jsx:10-39`
- `carmarq-control-frontend/src/Components/PrivateRoute.jsx:6-25`

### 2) Ordens de Serviço (núcleo)
- CRUD, listagem com filtros/paginação, status, descrição, reembolso, aprovação/rejeição de repasse, relatórios

Evidências:
- `src/main/java/com/example/Controller/ServiceOrderController.java:24-227`
- `src/main/java/com/example/Service/ServiceOrderService.java:49-674`
- `carmarq-control-frontend/src/Pages/Ordens.jsx:36-253`
- `carmarq-control-frontend/src/Pages/OrdemDetalhes.jsx:45-777`

### 3) Execução da OS
- Tempos: `TimeTracking`
- Peças: `ServicePart`
- Despesas: `ServiceExpense`
- Fotos: `ServicePhoto`

Evidências:
- `src/main/java/com/example/Controller/TimeTrackingController.java:17-49`
- `src/main/java/com/example/Controller/ServicePartController.java:17-51`
- `src/main/java/com/example/Controller/ServiceExpenseController.java:15-52`
- `src/main/java/com/example/Controller/ServicePhotoController.java:23-82`

### 4) Cadastros base
- Clientes, máquinas e usuários/equipe

Evidências:
- `src/main/java/com/example/Controller/ClientController.java:17-68`
- `src/main/java/com/example/Controller/MachineController.java:17-61`
- `src/main/java/com/example/Controller/UserController.java:17-90`

### 5) Dashboards e breakdown financeiro
- Dashboard por perfil com filtros de mês/ano
- Breakdown por card (`revenue`, `expenses`, `profit`, `pending`, `earnings`)

Evidências:
- `src/main/java/com/example/Controller/DashboardController.java:16-38`
- `src/main/java/com/example/Service/DashboardService.java:35-413`
- `carmarq-control-frontend/src/Components/Dashboards/*.jsx`

## Fluxos Importantes
### Fluxo 1: autenticação e autorização
1. Front chama `POST /api/auth/login` com `email/senha`.
2. Backend valida credenciais, bloqueio por tentativas e status ativo do usuário.
3. Backend emite cookie `accessToken`; frontend valida sessão via `POST /api/auth/validate`.
4. Rotas frontend usam `PrivateRoute`; backend reforça via `SecurityConfig` + `@PreAuthorize`.

Evidências:
- `AuthService.java:28-91`
- `JwtService.java:120-148`
- `AuthContext.jsx:10-39`
- `SecurityConfig.java:38-47`

### Fluxo 2: ciclo da OS
1. Criação (`/api/service-orders`) com cliente, máquina, técnico e tipo de serviço.
2. Técnico pode atuar em OS atribuída; proprietário/financeiro têm visão e ações ampliadas.
3. Evolução de status e fechamento/pagamento seguem regras no service.

Evidências:
- `ServiceOrderService.java:96-167`, `324-370`, `653-673`
- `ServiceOrderController.java:72-160`

### Fluxo 3: cálculo financeiro em cascata
1. Peças alteram `partsValue`.
2. Despesas alteram `displacementValue`/`expensesValue`.
3. Tempos alteram `travelValue` e (em manutenção) `serviceValue`.
4. Total, repasse técnico, imposto, taxa e lucro são calculados dinamicamente no mapeamento de DTO.

Evidências:
- `ServicePartService.java:65-67`, `86-88`, `107`
- `ServiceExpenseService.java:91-93`, `116-118`, `159-160`
- `TimeTrackingService.java:102-105`, `133-135`, `148-149`
- `ServiceOrderService.java:389-431`, `433-457`, `495-532`

### Fluxo 4: frontend → backend → banco
1. Frontend envia requests com `withCredentials: true`.
2. Backend autentica pelo cookie JWT e aplica regras de role/ownership.
3. Persistência via JPA/Hibernate em PostgreSQL.

Evidências:
- `carmarq-control-frontend/src/main.jsx:8-10`
- `JwtAuthenticationFilter.java:35-67`
- `application.properties:5-12`
- `ServiceOrderRepository.java:17-211`

## Regras de Negócio Identificadas
- Técnico, ao criar OS, sempre fica como responsável; proprietário define técnico explicitamente.
  - `ServiceOrderService.java:102-112`
- Em `INSTALACAO`, valor de serviço é travado pelo `installationPrice` da máquina.
  - `ServiceOrderService.java:136-139`
- Técnico não pode definir/editar campos financeiros sensíveis na criação/edição da OS.
  - `ServiceOrderService.java:128-134`, `221-235`
- Bloqueio de mutações em módulos operacionais quando OS está `PAGO` (e em alguns casos `CANCELADA`).
  - Peças: `ServicePartService.java:51-54`, `79-82`, `98-100`
  - Despesas: `ServiceExpenseService.java:61-63`, `104-106`, `127-129`
  - Fotos: `ServicePhotoService.java:58-61`, `101-104`
  - Tempos (UI e backend): `TabelaTempos.jsx:11`, `152-159`
- Reembolso por técnico tem limite de R$ 500 sem papel de gestão.
  - `ServiceOrderService.java:637-640`
- Regra de cálculo de deslocamento por tipo/origem (2.20 vs 2.50).
  - `ServiceExpenseService.java:165-174`
- Repasse técnico considera base líquida (dedução de 12% + taxa 3,50) + reembolso.
  - `ServiceOrderService.java:444-457`
- Conclusão da OS ajusta `closedAt` e move pagamento para `PENDENTE_APROVACAO`.
  - `ServiceOrderService.java:355-359`
- Lockout de login após 5 tentativas por 15 minutos.
  - `AuthService.java:49-56`
- Rate limit por IP, incluindo limite mais restrito para login.
  - `RateLimitingFilter.java:18-20`, `38-55`

Observações de consistência:
- Possível inconsistência de status entre frontend e backend (detalhes em “Pontos Críticos”).

## Banco de Dados
### Entidades/tabelas encontradas
- `usuarios`, `clients`, `machines`, `service_orders`, `service_parts`, `service_expenses`, `service_photos`, `time_trackings`

Evidências:
- `Usuario.java:21-23`
- `Client.java:15-16`
- `Machine.java:17-18`
- `ServiceOrder.java:17-23`
- `ServicePart.java:14-15`
- `ServiceExpense.java:10-11`
- `ServicePhoto.java:16-17`
- `TimeTracking.java:17-18`

### Relacionamentos
- `ServiceOrder` possui `ManyToOne` com cliente, máquina e técnico.
- `ServiceOrder` possui `OneToMany` com peças, despesas, fotos e tempos.

Evidências:
- `ServiceOrder.java:35-47`, `123-136`

### Consultas e agregações
- Repositório de OS contém alto volume de `@Query` com somatórios/contagens por status, mês/ano, técnico e pagamento.

Evidências:
- `ServiceOrderRepository.java:38-88`, `99-205`

### Estratégia de schema
- `spring.jpa.hibernate.ddl-auto=update`.
- Não foram encontrados arquivos de migration em `src/main/resources`.

Evidências:
- `application.properties:10`
- `src/main/resources` (somente `application.properties` e `static/logo-carmaq.png`)

Status de confirmação:
- Migrações externas (Flyway/Liquibase fora do repositório): **não confirmado**.

## Pontos Críticos
1. Possível inconsistência no fluxo de status.
- Frontend envia status como `EM_ANDAMENTO`, `REQUER_INSPECAO`, `COM_PROBLEMA` em ações do técnico.
- No service, técnico explicitamente só pode `CONCLUIDA`, `EM_REVISAO` ou `ABERTA`.
- Dashboard ainda agrega `EM_ANDAMENTO` e variações textuais legadas (`Em Andamento`, `Em Agendamento`).

Evidências:
- Frontend: `OrdemDetalhes.jsx:371-412`, `388-401`
- Backend: `ServiceOrderService.java:339-341`
- Dashboard: `DashboardService.java:166-179`

2. Divergência de autorização controller vs service para tempos.
- Controller permite `PROPRIETARIO` criar/editar/apagar tempo.
- Service bloqueia `PROPRIETARIO` em mutações de tempo.

Evidências:
- `TimeTrackingController.java:29-46`
- `TimeTrackingService.java:152-159`

3. Segurança operacional sensível em ambiente atual.
- `csrf` desabilitado com autenticação por cookie.
- Segredo JWT e credenciais de banco em `application.properties`.
- Usuários padrão com senhas previsíveis em bootstrap.

Evidências:
- `SecurityConfig.java:31`
- `application.properties:5-8`, `14-16`
- `DataInitializer.java:20-31`, `35-47`

4. Armazenamento de fotos em filesystem local.
- Upload em `./uploads/os_<id>` e leitura por caminho de disco.
- Área sensível para permissões, retenção e backup.

Evidências:
- `ServicePhotoService.java:33-35`, `71-76`, `107-119`

5. Camada de agregação financeira complexa e crítica.
- Regras financeiras distribuídas entre service, repository, dashboard e relatórios.
- Alto risco de divergência se fórmulas mudarem em apenas um ponto.

Evidências:
- `ServiceOrderService.java:433-457`, `495-532`
- `ServiceOrderRepository.java:103-205`
- `DashboardService.java:123-161`, `336-359`
- `ReportService.java:52`, `385`, `690`

6. Endpoints/módulos possivelmente subutilizados.
- Endpoint `PUT /api/service-orders/{id}/mark-received` existe no backend.
- Busca textual no frontend não encontrou uso direto.

Evidências:
- `ServiceOrderController.java:116-121`
- Busca no frontend por `mark-received`: **sem ocorrências**

Status: **não confirmado** se há uso por cliente externo fora deste frontend.

7. Estado atual do workspace impacta rastreabilidade.
- Há muitas alterações locais pendentes (`git status --short`), inclusive arquivos de frontend/backend e documentos.

Status: isso não impede análise estática, mas pode significar que partes estão em transição (**possível**).

## Próximos Passos Recomendados
1. Fechar um mapa oficial de máquina de estados da OS (status permitidos por role + transições válidas) e alinhar frontend/backend/repositórios/dashboard.
2. Fazer revisão dedicada do fluxo de autorização por endpoint vs service (especialmente `TimeTracking`, `ServiceOrder/status`, `mark-received`).
3. Consolidar fórmula financeira em um único núcleo de domínio reutilizado por dashboard, relatórios e listagens.
4. Endurecer configuração de segurança para produção (segredos fora do código, política CSRF/cookies, revisão CORS e bootstrap de usuários padrão).
5. Definir estratégia explícita de migração de banco (Flyway/Liquibase) para reduzir risco de drift de schema.
6. Revisar pipeline de fotos (validação, retenção, storage policy, backup e auditoria de acesso).
7. Validar cobertura de testes para fluxos críticos de negócio além dos testes atuais de cálculo.

---

## Apêndice de Evidências (Arquivos Mais Relevantes)
- Backend núcleo:
  - `src/main/java/com/example/Service/ServiceOrderService.java`
  - `src/main/java/com/example/Repository/ServiceOrderRepository.java`
  - `src/main/java/com/example/Service/DashboardService.java`
  - `src/main/java/com/example/Config/SecurityConfig.java`
  - `src/main/java/com/example/Controller/ServiceOrderController.java`
- Backend módulos de execução:
  - `ServicePartService.java`, `ServiceExpenseService.java`, `TimeTrackingService.java`, `ServicePhotoService.java`
- Frontend fluxo principal:
  - `carmarq-control-frontend/src/main.jsx`
  - `carmarq-control-frontend/src/contexts/AuthContext.jsx`
  - `carmarq-control-frontend/src/Pages/Ordens.jsx`
  - `carmarq-control-frontend/src/Pages/OrdemDetalhes.jsx`
  - `carmarq-control-frontend/src/Pages/NovaOS.jsx`
- Configuração e banco:
  - `pom.xml`
  - `src/main/resources/application.properties`
  - Entidades em `src/main/java/com/example/Models/*`
