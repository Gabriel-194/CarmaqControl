# Phase 2 Validation Strategy

## Dimensions Checked
- Dimension 8: Nyquist End-To-End Automation validation

### Test 1: Rate Calculation Matrix
- Input: OS with 2h work, Valentim origin vs Carmaq origin.
- Assertion: Valentim should be R$ 370 (2 * 185). Carmaq should be R$ 500 (2 * 250).

### Test 2: Technician Share
- Input: OS with R$ 500 labor, R$ 150 expenses.
- Assertion: Technician payout should be R$ 50 (10%) + R$ 150 (100%) = R$ 200.

### Test 3: Export Integrity
- Input: Call service to export Install Excel.
- Assertion: Returns > 0 byte array. Apache POI parses without error. Columns correspond to 'Item', 'Unid.', etc.
