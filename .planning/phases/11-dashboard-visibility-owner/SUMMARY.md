# Sumário de Execução - Fase 11: Dashboard de Lucratividade

A Fase 11 foi concluída, fornecendo ferramentas de monitoramento financeiro e operacional avançadas para a gestão da Carmarq.

## Alterações Realizadas

### 1. Visibilidade de Lucratividade
- **Listagem de OS**: Adicionada a coluna "Lucro" na página de Ordens de Serviço, visível apenas para `PROPRIETARIO` e `FINANCEIRO`. O valor é calculado subtraindo o repasse do técnico e reembolsos do valor total faturado.
- **Destaque Visual**: Valores de lucro positivo são exibidos em verde, enquanto prejuízos (se houver) aparecem em vermelho.

### 2. Monitoramento de Status Críticos
- **Backend (DashboardService)**: Implementada a contagem de OS com status `COM_PROBLEMA` (Com Problema), que agora é retornada no DTO de estatísticas.
- **Frontend (Dashboards)**: Adicionado o card "Com Problema" tanto no Dashboard do Proprietário quanto no Financeiro, facilitando a identificação imediata de serviços que necessitam de intervenção ou correção.

### 3. Filtros e Fluxos
- **Filtro Financeiro**: O dashboard financeiro agora reflete o total de ordens em `REQUER_INSPECAO` e `COM_PROBLEMA`, além dos pagamentos pendentes de aprovação.
- **Integridade de Dados**: Garantido que o cálculo de lucro considere o `discountValue` (desconto) aplicado à OS.

## Verificação
- **Interface**: Validada a exibição da coluna de Lucro em `Ordens.jsx`.
- **Navegação**: Verificado que o redirecionamento para detalhes mantém a visibilidade do lucro para administradores.
