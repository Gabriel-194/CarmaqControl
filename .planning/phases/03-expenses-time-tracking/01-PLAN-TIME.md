---
wave: 1
depends_on: []
files_modified:
  - carmarq-control-frontend/src/Components/TabelaTempos.jsx
  - src/main/java/com/example/Controller/ServiceOrderController.java
autonomous: true
---

# Phase 3: Expenses and Time Tracking V2

## Objective
Finalize Phase 3 requirements by making the live timer dynamic to support "Saída", "Trabalho", and "Retorno" and enforcing structural backend locks against "Instalação" time logging.

## Task 1: Make live timer dynamic for multiple time types
<read_first>
- carmarq-control-frontend/src/Components/TabelaTempos.jsx
</read_first>

<action>
Modify `TabelaTempos.jsx` state to include `const [timerType, setTimerType] = useState('TRABALHO')`.
Next to the "Iniciar Cronômetro" button, add a `<select>` that controls `timerType`:
`<option value="SAIDA_SEDE">Saída da Sede</option>`
`<option value="TRABALHO">Trabalho</option>`
`<option value="RETORNO_SEDE">Retorno à Sede</option>`

Update `handleStartTimer` to use `type: timerType` instead of hardcoded `'TRABALHO'` in the API payload.
</action>

<acceptance_criteria>
- `TabelaTempos.jsx` contains `<select value={timerType}`
- `handleStartTimer` uses `type: timerType`
</acceptance_criteria>

## Task 2: Enforce Installation Rule on Backend
<read_first>
- src/main/java/com/example/Controller/ServiceOrderController.java
</read_first>

<action>
In `ServiceOrderController.java`, find the `addTimeRecord` method (or similar mapping for `/api/service-orders/{id}/times` POST request).
Before saving or creating the time record, check if `order.getServiceType() != null && order.getServiceType().equals("INSTALACAO")`.
If true, return `ResponseEntity.badRequest().body("Não é permitido apontar horas para ordens de Instalação.")`.
Also protect the PUT requests (`/{id}/times/{timeId}`) with the same `INSTALACAO` block.
</action>

<acceptance_criteria>
- `ServiceOrderController.java` POST `/times` contains `if ("INSTALACAO".equals(order.getServiceType()))`
- Returns 400 Bad Request
</acceptance_criteria>

## Verification
1. Run backend tests.
2. Confirm frontend compiles.
