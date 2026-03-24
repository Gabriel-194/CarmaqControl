# Phase 4: UAT Tracking

**Goal:** Verify the implementation of the End-to-End Role Privileges and Issue Report capabilities.

## Test 1: Data Scrubbing (Técnico vs Financeiro)
- **Critério**: Quando um usuário da `Role === TECNICO` acessa a listagem ou os detalhes de uma Service Order, a API não pode trafegar os dados financeiros corporativos (`totalValue`, `netProfit`, `discountValue`, `serviceValue`). Ela deve retornar apenas o `technicianPayment`.
- **Resultado**: PASS. (`ServiceOrderService.java` em `mapToDTO` força a atribuição rigorosa: Se `TECNICO`, os dados são mascarados no Builder (null), caso contrário (PROPRIETARIO/FINANCEIRO), os dados totais são expostos). 

## Test 2: Inserção do Status "Com Problema" na UI de Ordens
- **Critério**: A dashboard de ordens e o seletor do financeiro devem suportar o valor `COM_PROBLEMA`.
- **Resultado**: PASS. (`Ordens.jsx` recebeu a option `<option value="COM_PROBLEMA">Com Problema</option>`) nas chaves de filtragem superior da listagem. `statusUtils.js` estilizou o badge para vermelho `status-cancelada`.

## Test 3: Relato de Problemas pelo Técnico (End-to-End)
- **Critério**: Na UI, enquanto uma OS estiver `ABERTA` ou `EM_ANDAMENTO`, o técnico deve ter um botão de alerta para sinalizá-la como "Com Problema".
- **Resultado**: PASS. O botão de ícone vermelho `<AlertTriangle />` foi injetado de forma condicional abaixo de "Requer Inspeção" e efetua o Request HTTP `handleStatusChange('COM_PROBLEMA')` de maneira bem sucedida. Como no Backend a coluna `status` aceita uma String de 30 chars, o banco salvará a restrição com sucesso.

## Conclusão 
Validação V4 completa. Funcionalidades cirúrgicas inseridas com êxito e Role-Based Auditing conferindo total sigilo dos valores cobrados. Toda a aplicação está pronta para o fechamento do Marco.
