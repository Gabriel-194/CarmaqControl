# Phase 5 Plan: Evolução do Sistema de OS

## 📝 Overview
This phase implements critical bugfixes, the 10% commission rule based on Total OS value, the "PAGO" status to lock financial data, and security enhancements for payloads.

## 🏗️ Execution Steps

### 1. Backend Core (Business Logic & Security)
- [ ] **Security:** Update `application.properties` with `server.tomcat.max-http-post-size=1MB`.
- [ ] **DTOs:** Add `@Size` annotations to `ServiceOrderRequestDTO`, `ServicePartRequestDTO`, and `ServiceExpenseRequestDTO`.
- [ ] **Commission Rule:**
    - Modify `ServiceOrderService.calculateTechnicianPayment` to use `(service + parts + expenses - discount) * 0.10`.
    - Implement `refreshTechnicianPayment` trigger in part/expense/hours updates.
- [ ] **Status Workflow:**
    - Add `PAGO` to `ServiceOrder` status logic.
    - Implement `PAGO` check in add/update/delete operations for parts and expenses.

### 2. Backend Endpoints (Edit/Delete)
- [ ] **Parts:** Add `PUT` and `DELETE` (with Pago check) to `ServicePartController`.
- [ ] **Expenses:** Add `PUT` (with Pago check) to `ServiceExpenseController`.
- [ ] **Photos:** In `ServicePhotosController` (check exact name), add check for status != `CONCLUIDA`.

### 3. Frontend (UI & Workflow)
- [ ] **TabelaTempos.jsx:** Remove "Iniciar/Parar" timer buttons. Highlight manual entry section.
- [ ] **OrdemDetalhes.jsx:**
    - Update Financial Card to show full breakdown and 10% calculation details.
    - Map the `PAGO` status to a visual badge.
- [ ] **ListaPecas/ListaDespesas.jsx:** Add "Editar" and "Excluir" buttons properly with Pago status check.

## ✅ Verification
- [ ] **Unit Tests:** Create `ServiceOrderCalculationTest.java`.
- [ ] **Manual UAT:**
    - Verify timer removal.
    - Verify 10% calculation with parts/expenses/discount.
    - Verify "PAGO" lock on edits.
    - Verify payload size block (test with 2000+ chars).
