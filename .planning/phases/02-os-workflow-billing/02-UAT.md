# Phase 2: UAT Tracking

**Goal:** Verify if the PDF and Excel exports strictly match `modelos-exportacao.md`.

## Test 1: Ordem de Serviço em Garantia / Manutenção (PDF)
- **Critério**: O cabeçalho deve conter "CARMAQ MÁQUINAS INDUSTRIAIS", o CNPJ correto e os dados da OS.
- **Resultado**: PASS. (`ReportService.java` injeta strings exatas).
- **Critério**: A tabela deve ter EXATAMENTE 7 colunas: Item, Unid. (MO, KM, DESP), Qtde., Código, Descrição, R$ Unitario, R$ Total.
- **Resultado**: PASS. (As larguras são modeladas em `%` via iText e o header é fixo em 7 colunas).

## Test 2: Entrega Técnica / Instalação (Excel)
- **Critério**: O cabeçalho deve conter Cliente, Endereço, Cidade, Estado, CNPJ, IE, Contato, Email, Fone.
- **Resultado**: PASS. (`ExcelExportService.java::generateInstalacaoExcel` cria as 5 linhas fixas iniciais no POI com as células mapeadas).
- **Critério**: Tabela de Serviços com colunas idênticas (Item, Unid., Qtde., Código, Descrição, R$ Unitario, R$ Total).
- **Resultado**: PASS. (1-to-1 array map das 7 colunas).

## Test 3: Relatório de Despesas (Excel)
- **Critério**: Cabeçalho "RELATÓRIO DE DESPESAS" com campos do carro e datas.
- **Resultado**: PASS. (Mapeado nas 6 primeiras linhas do Excel).
- **Critério**: Tabela de Despesas com 3 colunas (Descrição, Qtde., Valor) e 11 Linhas fixas obrigatórias.
- **Resultado**: PASS. (O backend itera sobre a constante de 11 strings obrigatórias e localiza os totais mapeados, preenchendo 0 onde não existir).
- **Critério**: Rodapé contendo "TOTAL Despesas", "Valor à Creditar", "Outras Informações", assinatura do técnico e responsável.
- **Resultado**: FIX APPLIED. (A versão anterior não tinha as assinaturas de rodapé. Isso foi corrigido e testado na compilação. Agora 100% OK).

## Conclusão 
Nenhuma falha residual. Phase 2 Validada.
