---
status: testing
phase: "14-business-logic-fixes"
source: ["Walkthrough de Auditoria e Correções"]
started: "2026-03-27T23:03:00-03:00"
updated: "2026-03-27T23:03:00-03:00"
---

## Current Test

number: 1
name: Registro de Tempo em Instalação
expected: |
  Acesse uma Ordem de Serviço de tipo "INSTALAÇÃO". Tente adicionar um registro de tempo (Início/Fim).
  O sistema deve permitir salvar o tempo sem exibir o erro "Não é permitido apontar horas para ordens de Instalação".
  Além disso, verifique se o valor do "Repasse Técnico" NÃO aumenta por causa dessas horas (pois em Instalação a comissão é fixa).
awaiting: user response

## Tests

### 1. Registro de Tempo em Instalação
expected: O sistema permite salvar tempos em OS de Instalação, mas o valor do repasse técnico permanece inalterado (comissão fixa).
result: [pending]

### 2. Edição de Despesas e Peças
expected: Clique para editar um valor de Deslocamento (KM) ou Peças em uma OS. Ao salvar, os novos valores devem persistir após o recarregamento da página.
result: [pending]

### 3. Relatórios Dinâmicos
expected: Gere um relatório PDF (Manutenção) ou Excel (Instalação). O nome do técnico e o modelo da máquina devem aparecer corretamente preenchidos, sem os espaços reservados "XXXX".
result: [pending]

### 4. Visibilidade Financeira (Role: TECNICO)
expected: Faça login como um Técnico. Acesse os detalhes de uma OS concluída. Você NÃO deve ver o "Lucro da Empresa" ou o "Total Geral do Pedido". Deve aparecer apenas o seu "Repasse Técnico".
result: [pending]

### 5. Verificação de "Frontend Burro"
expected: No console do navegador (Network tab), verifique a resposta da API ao carregar uma OS. Valores como `netProfit` e `totalValue` devem vir do Backend (mesmo que nulos para o técnico), e o Frontend não deve realizar somas ou cálculos de margem.
result: [pending]

### 6. Desbloqueio do Botão de Desconto
expected: Acesse uma OS com status "PAGO" ou "CONCLUIDA" como Proprietário. O botão "Aplicar" na seção de descontos deve estar habilitado e permitir a alteração e salvamento do valor.
result: [pending]

### 7. Encerramento de Sessão (Logout)
expected: Ao clicar em Sair, o sistema deve invalidar o cookie de acesso e redirecionar para a tela de login. Tentar voltar pelo histórico não deve carregar dados protegidos.
result: [pending]

## Summary

total: 7
passed: 0
issues: 0
pending: 7
skipped: 0

## Gaps

[none yet]
