# Requirements: Fase 6 - Refinamento de Regras Financeiras

## 🎯 Objetivo
Ajustar a lógica de cálculo financeiro para separar claramente o que é faturado ao cliente do que compõe a comissão do técnico. As despesas (hotel, alimentação, etc.) e custos de deslocamento (Km) devem ser cobrados do cliente mas não devem integrar a base de 10% do técnico.

## ⚙️ 1. Novas Regras de Cálculo (Backend)

### Total Faturado ao Cliente (`totalValue`)
O valor total deve ser a soma de:
- **Mão de Obra:** (Horas Trabalhadas × Valor Hora) - *Comissionável*
- **Tempo de Viagem:** (Horas de Viagem × Valor Hora Viagem) - *Comissionável*
- **Deslocamento (Km):** (Km Percorridos × Taxa Km, ex: 2.20) - *Não Comissionável*
- **Peças:** Soma de `ServicePart` - *Não Comissionável*
- **Despesas:** Soma de `ServiceExpense` (hotel, refeição, etc.) - *Não Comissionável*

### Repasse Técnico (`technicianPayment`)
A comissão do técnico deve ser calculada estritamente sobre a execução do tempo:
- **Fórmula:** `(Mão de Obra + Tempo de Viagem) × 0.10`
- **Exclusões:** Peças, Deslocamento (Km) e todas as Despesas Reembolsáveis.

## 🎨 2. Interface (Frontend)

### Nova OS (`NovaOS.jsx`) e Detalhes (`OrdemDetalhes.jsx`)
- O resumo financeiro deve separar visualmente:
    - Itens de Serviço (Mão de Obra + Viagem).
    - Itens Reembolsáveis (Km + Peças + Despesas).
- O rótulo da comissão técnica deve ser claro: "Repasse Técnico (10% sobre Serviços)".

## 🔒 3. Engenharia e Qualidade
- **Clean Code:** Manter a lógica de cálculo centralizada no `ServiceOrderService`.
- **Robustez:** Evitar `NullPointerException` ao lidar com despesas ou deslocamentos opcionais.
- **Nomenclatura:** Usar nomes claros para diferenciar `serviceValue`, `partsValue`, `travelValue`, `displacementValue` e `expensesValue`.
