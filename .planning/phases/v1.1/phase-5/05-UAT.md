---
status: testing
phase: v1.1/phase-5
source: [implementation_plan.md, walkthrough.md]
started: 2026-03-25T16:28:00Z
updated: 2026-03-25T16:28:00Z
---

## Current Test

number: 1
name: Cálculo de Repasse Técnico (10%)
expected: |
  Ao lançar uma OS com R$ 1000 de mão de obra e R$ 500 de peças, o resumo financeiro deve exibir "Repasse Técnico" no valor de R$ 150 (10% do total bruto).
awaiting: user response

## Tests

### 1. Cálculo de Repasse Técnico (10%)
expected: O valor de repasse ao técnico deve ser exatamente 10% do valor total da OS (Serviços + Peças + Despesas - Desconto).
result: [pending]

### 2. Status PAGO e Travas de Edição
expected: Ao alterar o status da OS para PAGO, os botões de adicionar/editar/remover em Peças e Despesas devem desaparecer ou ficar inativos.
result: [pending]

### 3. Edição de Peças e Despesas
expected: Ao clicar no ícone de editar (lápis) em uma peça ou despesa, os dados devem ser carregados no formulário superior para alteração. Após salvar, a lista e o total devem ser atualizados.
result: [pending]

### 4. Remoção do Cronômetro Automático
expected: Na aba de Tempos, o botão de "Iniciar Cronômetro"/"Parar Atividade" não deve mais existir. Deve haver apenas o botão de "Lançar Tempo".
result: [pending]

### 5. Resumo Financeiro Detalhado
expected: O painel financeiro na OrdemDetalhes deve exibir explicitamente: Total Bruto, Total Peças, Total Despesas, Desconto, Repasse Técnico (10%) e Lucro Carmarq.
result: [pending]

### 6. Limite de Payload (Segurança)
expected: Ao tentar salvar uma descrição de serviço muito longa (acima de 2000 caracteres), o sistema deve retornar um erro de validação amigável e impedir o salvamento.
result: [pending]

## Summary

total: 6
passed: 0
issues: 0
pending: 6
skipped: 0

## Gaps

[none yet]
