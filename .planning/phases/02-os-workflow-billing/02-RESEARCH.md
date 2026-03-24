# Phase 2: OS Workflow & Billing - Research

## Billing Mappings
- **Carmaq Maintenance**: R$ 250,00/h
- **Valentim (Garantia) Maintenance**: R$ 185,00/h
- **Travel Time (Hora Deslocamento)**: R$ 85,00/h
- **Distance / KM (Quilômetro)**: R$ 2,20/km

*Current `ServiceOrderService.java` uses a generic `serviceValue` and doesn't differentiate origins precisely in calculation. `TimeTracking` handles hours. There's a `manutencaoOrigin` field in `ServiceOrder` that can be used to switch between 250 and 185 hourly rates.*

## Technician Payment
- Technician retains 10% of Labor (`MO` / `serviceValue`).
- Technician retains 100% of validated Expenses (`DESP` / `expensesValue`).

## Official Reporting Strategy
1. **Ordem de Serviço (PDF)**:
   - Uses `iText7`. Target: `ReportService.java`.
   - Implement the specific tables with 7 columns: Item | Unid. | Qtde. | Código | Descrição | R$ Unitario | R$ Total.
2. **Entrega Técnica / Instalação (Excel)**:
   - Uses `Apache POI`. Target: `ExcelExportService.java`.
   - Uses the exact same 7 columns, plus a specific header with Client, City, State, CNPJ, IE.
3. **Relatório de Despesas (Excel)**:
   - Uses `Apache POI`. Target: `ExcelExportService.java`.
   - Explicit fixed rows must always be rendered: Refeição, Hotel, Passagem Aérea, Taxi, Pedágio, Combustível, Estacionamento, Aluguel Carro, Quilometragem, Desp. com Material, Outros.

## Technical Validation (Validation Architecture)
- `testCalculateTotalWithOrigins`: Verify 250 vs 185 math.
- `testTechnicianPaymentCalculation`: Verify 10% MO + 100% EXP.
- Generate actual PDF/Excel bytes in a mock to ensure POI and iText don't throw NPEs during template rendering.
