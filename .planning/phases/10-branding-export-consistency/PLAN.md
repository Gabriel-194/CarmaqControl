# Plano de Implementação - Fase 10: Branding e Consistência de Exportação

Esta fase foca em padronizar o visual de todos os documentos gerados pelo sistema, garantindo que a identidade visual da Carmarq (Logo e Verde) esteja presente de forma profissional e consistente.

## Melhorias Propostas

### 1. Padronização de Excel (XLSX)
- **Unificação do Logotipo**: Remover a lógica redundante de carregamento de imagem dentro dos métodos `generateInstallationXlsx` e `generateExpensesXlsx`. Ambos passarão a usar exclusivamente o helper `addLogoToExcel`.
- **Cabeçalho Corporativo**: Garantir que as informações da empresa no Excel estejam alinhadas à direita, deixando espaço para o logo à esquerda, similar ao layout do PDF.
- **Cores Oficiais**: Assegurar que os banners das seções (ex: "RELATÓRIO DE DESPESAS", "EQUIPAMENTO") utilizem a cor `XLS_GREEN_DARK`.

### 2. Refatoração do ReportService.java
- Limpeza dos métodos `generateInstallationXlsx` e `generateExpensesXlsx`.
- Ajuste no posicionamento da âncora do logo no Excel para não sobrepor dados importantes.

## Alterações no Código

#### [MODIFY] [ReportService.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Service/ReportService.java)
- Simplificar `generateInstallationXlsx` para usar `addLogoToExcel`.
- Simplificar `generateExpensesXlsx` para usar `addLogoToExcel`.
- Ajustar os estilos de cor para usar `XLS_GREEN_DARK` de forma consistente.

## Plano de Verificação

### Testes Manuais
1. **Relatório de Instalação**: Gerar o Excel e verificar se o logo aparece no canto superior esquerdo e se o cabeçalho está em verde escuro.
2. **Relatório de Despesas**: Gerar o Excel e verificar a consistência visual com o de instalação.
3. **Verificação de Regressão**: Garantir que o PDF de Manutenção não foi afetado negativamente.
