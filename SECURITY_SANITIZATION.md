# SECURITY REVIEW - Sanitização e Exposição de Dados

## 1. Auditoria de DTOs de Saída

### Vazamentos Identificados
- **Usuario.java**: A classe possui `senha`, `failedLoginAttempts` e `accountLockedUntil`. Se for serializada diretamente (ex: via `ResponseEntity<Usuario>`), esses campos vazam.
- **LoginResponseDTO**: Deve ser verificado se o token JWT contém informações excessivas no Payload (claims).

### Recomendações de Hardening
1.  **Entidade Usuario**: Adicionar `@JsonIgnore` em `senha`, `failedLoginAttempts`, `accountLockedUntil`.
2.  **Validação de Entrada**: Garantir que as validações `@NotNull` e `@Size` estão em todos os DTOs de Request.

## 2. Auditoria de Lógica "Smart Backend" vs "Dumb Frontend"

### Mudanças Requeridas
- **Frontend**: Verificar se o cálculo de percentual do técnico (10%) ou custos totais está sendo feito no JS.
- **Backend**: Mover toda lógica de cálculo financeiro para os Services e retornar apenas o resultado final formatado.

## 3. Checklist de Implementação
- [ ] Ocultar `senha` em `Usuario.java` com `@JsonIgnore`.
- [ ] Ocultar campos de segurança interna em `Usuario.java`.
- [ ] Validar que `UserResponseDTO` não expõe segredos.
- [ ] Confirmar que o frontend não contém validações complexas replicadas.
