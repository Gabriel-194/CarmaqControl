# Phase 12: Hardening: Correção de BOLA e Mass Assignment - Context

**Gathered:** 2026-03-26
**Status:** Ready for planning

<domain>
## Phase Boundary

Esta fase foca em resolver as vulnerabilidades críticas de segurança identificadas no relatório de auditoria (Vulnerabilidades 1, 2 e 3):
1. Vazamento de dados financeiros para técnicos na criação de OS.
2. Alteração maliciosa de valores e descontos (Mass Assignment) por técnicos.
3. Acesso não autorizado a sugestões de máquinas (BOLA).

</domain>

<decisions>
## Implementation Decisions

### D-01: Controle de Visibilidade na Criação de OS
- No método `createServiceOrder` do `ServiceOrderService`, a resposta enviada ao frontend deve usar o `role` do usuário autenticado no `mapToDTO`.
- Jamais usar o literal `"PROPRIETARIO"` fixo no retorno, a menos que o usuário seja de fato um Proprietário.

### D-02: Prevenção de Mass Assignment (Campos Financeiros)
- No `ServiceOrderService`, antes de criar/salvar a OS, os campos `serviceValue`, `discountValue`, `travelValue` e `displacementValue` do DTO devem ser ignorados se o usuário for `TECNICO`.
- Valores para técnicos devem ser sempre `0.0` na criação, exceto se houver lógica de automação (como o preço de instalação para máquinas).

### D-03: Proteção BOLA em Sugestões de Máquina
- No `ServiceOrderController.getSuggestions`, adicionar uma verificação de permissão.
- Se o usuário for `TECNICO`, ele só pode obter sugestões para uma `machineId` se ele possuir pelo menos uma Ordem de Serviço (em qualquer status) vinculada a essa máquina ou ao cliente dono da máquina.
- Se for `PROPRIETARIO` ou `FINANCEIRO`, o acesso é irrestrito.

### the agent's Discretion
- A implementação exata da query de verificação de histórico do técnico para o BOLA das máquinas fica a critério do desenvolvedor (pode usar o `ServiceOrderRepository`).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Security
- `security_audit_report.md` — Relatório base com as vulnerabilidades detalhadas.
- `src/main/java/com/example/Service/ServiceOrderService.java` — Alvo principal das correções.
- `src/main/java/com/example/Controller/ServiceOrderController.java` — Alvo da correção BOLA.

</canonical_refs>
