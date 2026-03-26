# Context: Fase 6 - Refinamento de Regras Financeiras

Este documento formaliza as decisões de design e regras de negócio para a Fase 6, servindo de guia para a implementação no backend e frontend.

## ⚖️ Regras de Cálculo Financeiro

### 1. Separação de Bases de Custo
Diferenciamos agora explicitamente o que é **trabalho humano** (comissionável) do que é **custo operacional/logístico** (reembolsável pelo cliente, pago pela empresa).

| Categoria | Descrição | Comissionável (10%)? |
| :--- | :--- | :---: |
| **Mão de Obra** | Horas trabalhadas no cliente. | Sim |
| **Tempo de Viagem** | Horas em deslocamento (técnico está em serviço). | Sim |
| **Deslocamento (Km)** | Custo por Km rodado (veículo/combustível). | Não |
| **Peças** | Peças trocadas durante o atendimento. | Não |
| **Despesas** | Hotel, alimentação, passagens, etc. | Não |

---

## 🛠️ Decisões Técnicas (Backend)

### Modelo de Dados (`ServiceOrder`)
Adicionaremos dois novos campos persistidos para garantir o histórico e precisão dos relatórios:
- `travel_value` (Double): Valor faturado pelo tempo de viagem.
- `displacement_value` (Double): Valor faturado pelos Km rodados.

### Lógica de Totais (`ServiceOrderService`)
- `totalValue` (Billed to Client) = `serviceValue` (Labor) + `travelValue` + `displacementValue` + `partsValue` + `expensesValue` - `discountValue`.
- `technicianPayment` = (`serviceValue` + `travelValue`) * 0.10.

---

## 🎨 Decisões de Interface (Frontend)

### Resumo Financeiro (`OrdemDetalhes.jsx`)
O resumo será dividido em dois blocos claros:
1. **Itens de Serviço (Base de Comissão)**: Mão de Obra + Viagem.
2. **Custos Adicionais/Reembolsos**: Km + Peças + Despesas.
3. **Cálculo de Repasse**: Exibir explicitamente "10% de [Soma dos Serviços]".

### Cadastro de OS (`NovaOS.jsx`)
Adição de campos para entrada manual (ou sugestão) de:
- Valor de Viagem (R$).
- Valor de Deslocamento (Km).

---

## ❓ Questões em Aberto
- **Taxa de Km:** Atualmente o `TravelCalculationService` usa R$ 10.00/km como "costPerKm". O usuário mencionou R$ 2.20 em conversa. **Decidido: Usaremos R$ 2.20 como padrão**, mas manteremos o campo editável no backend/frontend.
- **Relatórios (PDF/Excel):** Os relatórios devem ser atualizados na Fase 6 para refletir essa nova separação? (Presumido: Sim, na Fase 7 ou como parte desta fase se sobrar tempo).

---
**Status:** Aguardando revisão do usuário.
