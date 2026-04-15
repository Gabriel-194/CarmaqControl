# Revisão de Segurança - Correção da Lógica dos Dashboards

## 1. Autorização e BOLA
O Dashboard possui verificações rígidas de papel (`role`) e ID de usuário no `DashboardService`.
- **Técnicos**: Só acessam as métricas através de métodos filtrados pelo seu próprio `technician_id` (ex: `countByTechnician`).
- **Administrador/Financeiro**: Possuem permissão total para ver o faturamento global e lucro líquido da empresa.

## 2. Prevenção de Divulgação de Dados Sensíveis
As novas fórmulas matemáticas no banco de dados devem garantir que o técnico não consiga, por inferência, obter valores brutos se eles não forem mostrados. No entanto, como o técnico já vê seus detalhes em cada OS que criou, o cálculo agregado no Dashboard é seguro.

## 3. Robustez dos Cálculos (Sanitização)
- **Null Safety**: Todas as colunas numéricas no banco podem conter `NULL` (especialmente em registros legados). Utilizaremos `COALESCE(?, 0.0)` em todas as somas JPQL para evitar que um único campo nulo invalide o cálculo de toda a OS ou do período.
- **Divisão por Zero**: As fórmulas propostas não envolvem divisão direta por campos do banco de dados (apenas percentuais constantes), eliminando riscos de exceções matemáticas que poderiam derrubar o serviço de dashboard.

## 4. Conclusão
A implementação proposta é robusta e respeita os níveis de acesso definidos no CarmarqControl Briefing.
