---
wave: 2
depends_on: ["1-PLAN-BILLING.md"]
files_modified:
  - "src/main/java/com/example/Service/ReportService.java"
  - "src/main/java/com/example/Service/ExcelExportService.java"
  - "src/main/java/com/example/Controller/ServiceOrderController.java"
  - "carmarq-control-frontend/src/Pages/OrdemDetalhes.jsx"
autonomous: true
---
# Plan: Official Export Templates Implementation

<objective>
Refactor ReportService and ExcelExportService to implement the pixel-perfect layout defined in `modelos-exportacao.md`.
</objective>

<task>
<read_first>
- modelos-exportacao.md
- src/main/java/com/example/Service/ReportService.java
- src/main/java/com/example/Service/ExcelExportService.java
</read_first>
<action>
1. **PDF Manutenção (`ReportService.java`)**: 
   - Overhaul `generateManutencaoReport`. 
   - Add hardcoded Header: "CARMAQ MÁQUINAS INDUSTRIAIS...", "CNPJ: 60.526.327/0001-23 ... Araucária | Paraná".
   - Replace old boxes with exactly 1 Table with 7 headers: "Item", "Unid. (MO, KM, DESP)", "Qtde.", "Código", "Descrição", "R$ Unitario", "R$ Total".
   - Map `TimeTracking` rows as items (MO), `ServiceExpense` as items (DESP/KM).
2. **Excel Instalação (`ExcelExportService.java`)**:
   - Create `public byte[] generateInstalacaoExcel(ServiceOrder order)`.
   - Write Client data (Nome, Endereço, Cidade, Estado, CNPJ, IE, etc.) in rows 1-5.
   - Write headers exactly: "Item" | "Unid." | "Qtde." | "Código" | "Descrição" | "R$ Unitario" | "R$ Total".
3. **Excel Despesas (`ExcelExportService.java`)**:
   - Create `public byte[] generateDespesasExcel(ServiceOrder order)`.
   - Write header rows for "RELATÓRIO DE DESPESAS".
   - Define array of fixed rows: `String[] fixed = {"Refeição", "Hotel", "Passagem Aérea", "Taxi", "Pedágio", "Combustível", "Estacionamento", "Aluguel Carro", "Quilometragem", "Desp. com Material", "Outros"}`. Render them on the sheet. Search the DB for values matching these categories, injecting values where matched, 0.00 otherwise.
4. **Backend/Frontend Link**: Add specific endpoints `/api/orders/{id}/export/instalacao-excel` and `/api/orders/{id}/export/despesas-excel`. Update `OrdemDetalhes.jsx` in frontend to have buttons for these.
</action>
<acceptance_criteria>
- `ReportService.java` contains string `"60.526.327/0001-23"`.
- `ExcelExportService.java` contains array layout for `"Refeição"`, `"Hotel"`, etc.
- `ExcelExportService.java` contains `public byte[] generateInstalacaoExcel`.
- `OrdemDetalhes.jsx` contains minimum 3 buttons mapping to the new export logic.
</acceptance_criteria>
</task>
