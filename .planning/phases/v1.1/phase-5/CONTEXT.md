# Phase 5 Context: Evolução do Sistema de OS

## 🎯 Goals
Implement bugfixes, new business rules (10% commission, PAGO status), interface improvements (manual time entry, detailed breakdown), and security measures (payload limits).

## 🛠️ Decisions

### 1. Calculation Logic
- **Technician Commission:** 10% of the **Total OS Value**.
  - `Total OS = Service Value + Parts Value + Expenses Value - Discount`.
  - Trigger: Recalculate whenever parts, expenses, or time records are modified.
- **Persistence:** The calculated value will be stored in `ServiceOrder.technicianPayment` to ensure consistency after the OS is closed/paid.

### 2. Status Workflow
- **New Status:** `PAGO`.
- **Transitions:**
  - `FINANCEIRO` or `PROPRIETARIO` can set to `PAGO` via the payment approval flow.
- **Restrictions:**
  - If status is `PAGO`: Editing or deleting Parts and Expenses is strictly blocked.
  - If status is `CONCLUIDO`: Photo management is blocked.

### 3. Interface Changes
- **Times:** Remove the "Timer" (start/stop) UI. Keep only "Manual Entry".
- **Breakdown:** Ensure `OrdemDetalhes.jsx` shows all individual items (items, parts, expenses) and the clear 10% calculation to Admin/Financial.

### 4. Security
- **Global limit:** `server.tomcat.max-http-post-size=1048576` (1MB) for text payloads.
- **DTO Validation:** Implement `@Size` annotations in all relevant DTOs:
  - `ServicePartRequestDTO`: `partName` (100), `description` (500).
  - `ServiceExpenseRequestDTO`: `description` (500).
  - `ServiceOrderRequestDTO`: `problemDescription` (2000), `serviceDescription` (2000).

## 📅 Next Steps
1. Create `Phase 5 Implementation Plan`.
2. Implement backend changes (Status, Commission logic, Edit/Delete endpoints).
3. Implement frontend changes (Remove timer, Detail visibility).
4. Verify via manual UAT.
