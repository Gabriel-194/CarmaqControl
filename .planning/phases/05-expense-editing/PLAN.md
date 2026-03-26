# Plano de Implementação - Fase 05: Edição de Despesas

Esta fase foca em permitir que usuários com papel de **Proprietário** ou **Financeiro** editem despesas mesmo após a OS ter sido marcada como `CONCLUIDA` pelo técnico. Atualmente, a edição está restrita ao status `EM_ANDAMENTO`.

## Alterações Propostas

### Backend (Spring Boot)

#### [MODIFY] [ServiceExpenseService.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Service/ServiceExpenseService.java)
- Alterar as validações nos métodos `addExpense`, `updateExpense` e `removeExpense`.
- **Regra:** Bloquear edição apenas se o status for `PAGO` ou `CANCELADA`.
- Se o status for `CONCLUIDA`, permitir edição apenas se o usuário for `PROPRIETARIO` ou `FINANCEIRO`. Técnicos continuam bloqueados após a conclusão.

### Frontend (React)

#### [MODIFY] [ListaDespesas.jsx](file:///c:/Users/gabri/Downloads/CarmaqControl/carmarq-control-frontend/src/Components/ListaDespesas.jsx)
- Atualizar a lógica de `isEditable`.
- No momento, `isEditable = orderStatus === 'EM_ANDAMENTO'`.
- **Nova lógica:** 
    - Se `user.role` for `TECNICO`, `isEditable` permanece apenas em `EM_ANDAMENTO`.
    - Se `user.role` for `PROPRIETARIO` ou `FINANCEIRO`, `isEditable` deve ser verdadeiro desde que o status não seja `PAGO` ou `CANCELADA`.

## Plano de Verificação

### Testes Manuais
- Logar como Técnico e tentar editar despesa em OS `CONCLUIDA` (Deve falhar).
- Logar como Proprietário/Financeiro e editar despesa em OS `CONCLUIDA` (Deve funcionar).
- Verificar se o `Total Faturado` e o `Lucro` na tela de detalhes da OS são atualizados após a edição da despesa.
