# PRD - Correção da Lógica dos Dashboards (Admin e Técnico)

## 1. Visão Geral
Os dashboards de Administrador e Técnico estão apresentando valores incorretos devido a falhas na lógica de cálculo e filtragem de status no banco de dados. Os cálculos atuais ignoram descontos, utilizam regras de repasse defasadas (10% fixo sobre valores brutos) e deixam de contabilizar ordens de serviço quando estas mudam de status para "PAGO".

## 2. Problemas Identificados
1.  **Cálculo de Faturamento (Admin)**: Não subtrai o `discount_value`.
2.  **Repasse do Técnico (Admin/Técnico)**: Utiliza a lógica antiga (10% de Mão de Obra + Viagem) em vez da lógica atual (10% sobre a Base Líquida pós impostos e taxas).
3.  **Filtro de Status**: Consultas de faturamento e lucro olham apenas para o status `CONCLUIDA`. Ao marcar como `PAGO`, a OS desaparece das métricas.
4.  **Sincronização**: O Repository e o Service possuem lógicas divergentes. A "verdade" deve ser o que está no `ServiceOrderService`.

## 3. Requisitos Funcionais
- **Lógica de Faturamento**: `Total = (Mão de Obra + Viagem + Deslocamento + Peças + Despesas) - Desconto`.
- **Lógica de Repasse**: `Repasse = (Faturamento - 12% impostos - 3,50 taxa boleto) * 10%`.
- **Inclusão de Status**: Métricas de faturamento e lucro devem considerar OS nos status `CONCLUIDA` e `PAGO`.
- **Dashboard do Técnico**: "A Receber" deve refletir o valor real considerando a fórmula acima e o status de pagamento `PENDENTE_APROVACAO` ou `A_RECEBER`.
- **Dashboard do Admin**: Deve refletir o lucro líquido real considerando faturamento total menos repasses e reembolsos.

## 4. Requisitos Não Funcionais
- **Consistência**: Centralizar a lógica de cálculo para evitar divergências futuras.
- **Performance**: Manter o Dashboard rápido, mesmo com a complexidade adicional dos cálculos.

## 5. Expectativas de Lógica de Negócios
- OS com status `PAGO` devem continuar sendo contabilizadas nas receitas e lucros históricos.
- Descontos devem ser aplicados antes de qualquer cálculo de imposto ou comissão.
- O valor "A Receber" do técnico deve ser baseado no status `technicianPaymentStatus` (`A_RECEBER` ou `PENDENTE_APROVACAO`) e não apenas no status da OS.
