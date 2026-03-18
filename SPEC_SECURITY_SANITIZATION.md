# SPEC - Sanitização e Hardening de Respostas

## 1. Mapeamento de DTOs e Mudanças Necessárias

### Entidade: Usuario / DTOs Relacionados
- **UserResponseDTO**:
    - [KEEP] id, nome, email, telefone, role, ativo.
    - [REMOVE] campos internos de auditoria se existirem.
- **LoginResponseDTO**:
    - [KEEP] success, message, token, role, userId, userName.
    - [CHECK] Garantir que o objeto `Usuario` completo não está sendo retornado por engano.

### Entidade: ServiceOrder / DTOs Relacionados
- **ServiceOrderResponseDTO**:
    - [REMOVE] Campos de cálculo bruto que devem ser processados no Backend.

## 2. Regras de Serialização
- Utilizar `@JsonIgnore` nas Entidades para campos que nunca devem sair do backend (ex: `senha`, `failedLoginAttempts`).
- Garantir que todos os controladores utilizem DTOs em vez de retornar entidades `JPA` diretamente.

## 3. Validação Centralizada
- Utilizar `Jakarta Bean Validation` (`@NotBlank`, `@Size`, `@Pattern`) nos DTOs de Request.
- Remover qualquer regra de "validação de senha" ou "validação de email" complexa do frontend que cause duplicação de lógica.

## 4. Endpoints de Auditoria
- Revisar `GET /api/users` para garantir que a lista de usuários não inclua hashes de senha ou status de bloqueio.
