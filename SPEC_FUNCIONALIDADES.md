# SPEC: Técnico Pode Criar OS & Despesas da OS

## Database Model

**ServiceExpense**
*   `id` (Long, PK)
*   `service_order_id` (Long, FK not null)
*   `expense_type` (String/Enum, not null)
*   `quantity_km` (Double)
*   `value` (Double, not null)
*   `description` (String 300)

**ServiceOrder (Modificações)**
*   Remover `travel_cost`
*   Adicionar `expenses_value` (Double, default 0.0)

## Backend Architecture

### Entities
*   `ExpenseTypeEnum`: DESLOCAMENTO_KM, PEDAGIO, ALIMENTACAO, HOSPEDAGEM, OUTRO
*   `ServiceExpense`: Mapping ManyToOne -> ServiceOrder

### Repositories
*   `ServiceExpenseRepository`: findByServiceOrderId e Query `sumTotalByServiceOrderId`

### Services
*   `ServiceOrderService`: 
    *   Método `createServiceOrder`: Validação do context (Técnico X Prorietário).
    *   Método `refreshExpensesValue`: Pega o somatório do `ExpenseRepository`, atualiza a respectiva Ordem de Serviço e persiste no repositório com o status atualizado do gasto.
    *   Método `calculateTechnicianPayment`: Atualizado para (serviceValue * 0.10) + expensesValue.
*   `ServiceExpenseService`: `getExpensesByServiceOrderId`, `addExpense`, `removeExpense`. Validação de EM_ANDAMENTO necessária.

### Controllers
*   `ServiceOrderController`: `POST /api/service-orders` (role: PROPRIETARIO or TECNICO).
*   `ServiceExpenseController`: GET, POST e DELETE em `/api/service-orders/{id}/expenses`.

### DTOs
*   `ServiceOrderRequestDTO`: `technicianId` sem validação `@NotNull`.
*   `ServiceExpenseRequestDTO`: `expenseType`, `quantityKm`, `value`, `description`.
*   `ServiceExpenseResponseDTO`: Retorna info e `expenseTypeLabel` (português formatado).
*   `ServiceExpenseListDTO`: Lista e total em valor.

## Security Model
*   `TECNICO` ganha permissão de POST `ServiceOrder` e nas chamadas de `/expenses`.

## Frontend Structure

### Pages
*   `NovaOS.jsx`: Alteração para verificar se Request é de `TECNICO` (não mostra Select) ou não. Adição do label de "valor sugerido".
*   `OrdemDetalhes.jsx`: Adicionar Tab 'Despesas'.
*   `Ordens.jsx`: Mostrar botão de criação para técnicos.
*   Rotas e Sidebars integradas em `main.jsx` e `Sidebar.jsx`.

### Components
*   `ListaDespesas.jsx`: Formulário e listagem das despesas de uma OS. Respeitando Regras das Enums.
*   `ListaDespesas.css`: Estilização respectiva ao componente.

## Routing
*   A rota protegida em `/nova-os` foi expandida para incluir role de técnico (`['PROPRIETARIO', 'TECNICO']`).
*   As APIs Axios usam `{ withCredentials: true }`.
