# Phase 2: OS Workflow & Billing - Context

## Phase Boundary
Deliver a robust billing and export system that strictly follows the Carmarq official templates for PDF and Excel. Implement origin-based hourly billing (Carmaq vs Valentim/Warranty) and technical travel reimbursement rules.

## Implementation Decisions

### Billing & Rates
- **Maintenance Rates:** 
    - Carmarq: R$ 250,00/h.
    - Valentim (Garantia): R$ 185,00/h.
    - Deslocamento: R$ 85,00/h (Need to confirm if this is the standard).
- **Travel Cost:** R$ 2,20 per KM (Technician reimbursement).
- **Technician Payment:** 10% of total labor (MO) + 100% of validated expenses (DESP/KM).

### Export Strategy
- **PDF (iText7):** Used for "Ordem de Serviço em Garantia / Manutenção". Must include pixel-perfect header with company CNPJ/Address.
- **Excel (Apache POI):**
    - "Entrega Técnica / Instalação": Structured table with specific columns.
    - "Relatório de Despesas": Fixed rows for standard expense types (Refeição, Hotel, etc.).

### UI/Frontend
- **Nova OS:** Improved search select for clients.
- **OS Details:** Buttons for specific exports:
    - `Exportar PDF (Manutenção)`
    - `Exportar Excel (Instalação)`
    - `Relatório de Despesas`
- **Workflow:** OS status transitions must trigger financial lock-in.

### Discretion
- **Architecture:** Keep the `ReportService.java` but refactor into specialized methods/classes for each template.

## Canonical References
- `modelos-exportacao.md` (Design Source of Truth)
- `src/main/java/com/example/Service/ReportService.java`
- `src/main/java/com/example/Service/ExcelExportService.java`
- `src/main/java/com/example/Service/ServiceOrderService.java`
- `carmarq-control-frontend/src/Pages/NovaOS.jsx`
