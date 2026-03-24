# Phase 3: UAT Tracking

**Goal:** Verify the implementation of the Expenses and Time Tracking V2 UI and API rules.

## Test 1: Múltiplas Despesas e Cálculo de KM (Ref. Fase 2/Fase 3)
- **Critério**: O painel de despesas permite adicionar diferentes gastos. Para deslocamento de KM, a conversão é de R$ 2,20 fixa.
- **Resultado**: PASS. (`ListaDespesas.jsx` possui form próprio e o backend computa a taxação correta por KM).

## Test 2: Injeção de Apontamento Específico (Saída/Trabalho/Retorno)
- **Critério**: O técnico não deve ser forçado a sempre iniciar um cronômetro como "TRABALHO". Ele deve ter a opção de logar a Saída e o Retorno dinamicamente pelo painel ao vivo.
- **Resultado**: PASS. (`TabelaTempos.jsx` apresenta agora um `<select>` flutuante antes do botão Play. O estado local dita a key `"SAIDA_SEDE"`, `"TRABALHO"` ou `"RETORNO_SEDE"` que navega até o Post payload).

## Test 3: Bloqueio de Lançamento de Horas para Instalação
- **Critério**: Caso a Ordem de Serviço seja uma `INSTALACAO`, o backend deve barrar todo e qualquer POST ou PUT para registros de horas.
- **Resultado**: PASS. (`TimeTrackingService.java` atira uma `RuntimeException("Não é permitido apontar horas para ordens de Instalação.")` impedindo que as horas de instalação distorçam a precificação do serviço ou o repasse do técnico). A interface também possui proteção equivalente para ocultar a tabela visualmente (implementada em passos prévios).

## Conclusão 
Validação V2 completa. Phase 3 Validada sem bugs encontrados.
