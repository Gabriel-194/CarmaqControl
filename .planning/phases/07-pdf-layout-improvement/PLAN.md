# Plano de Implementação - Fase 07: Layout do PDF de Manutenção

O objetivo desta fase é tornar o PDF de manutenção mais profissional, intuitivo e prático para o cliente final.

## Melhorias de Design Propostas

### 1. Reestruturação do Cabeçalho
- Adicionar uma linha horizontal sutil após os dados da empresa.
- Melhorar o alinhamento do Logo com o bloco de endereço.

### 2. Agrupamento de Informações
- Criar seções claras: **Dados do Cliente**, **Informações da Máquina** e **Relatório de Execução**.
- Utilizar bordas consistentes e cores de fundo suaves para as labels.

### 3. Tabela de Itens (Faturamento/Serviço)
- Refinar as colunas para que a descrição ocupe mais espaço.
- Destacar os totais (Mão de Obra, Deslocamento, Peças) de forma mais legível.

### 4. Campos de Assinatura
- Adicionar campos no final do documento para:
  - **Assinatura do Técnico**
  - **Assinatura do Cliente** (com campo para data e RG/Nome legível).

### 5. Rodapé Institucional
- Garantir que as informações de contato e bancárias estejam organizadas em um grid mais limpo.

## Alterações no Código

#### [MODIFY] [ReportService.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Service/ReportService.java)
- Atualizar o método `generateMaintenancePdf`.
- Implementar as novas tabelas e estilos do iText7.

## Plano de Verificação

### Testes Manuais
1. Gerar PDF de uma OS concluída.
2. Validar se o logo Valentin (para garantias) aparece corretamente sem quebrar o layout.
3. Verificar a legibilidade das assinaturas ao imprimir ou visualizar em dispositivos móveis.
