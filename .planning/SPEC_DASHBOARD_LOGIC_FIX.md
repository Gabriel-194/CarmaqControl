# Especificação Técnica - Correção da Lógica dos Dashboards

## 1. Arquitetura Backend

### Alterações no Repositório: `ServiceOrderRepository.java`

As consultas de agregação devem ser corrigidas para refletir as regras de negócio reais, incluindo descontos e multiplicidade de status.

#### Definição das Fórmulas em SQL (JPQL):
- **Faturamento Bruto (TotalValue)**: `(COALESCE(so.serviceValue, 0) + COALESCE(so.travelValue, 0) + COALESCE(so.displacementValue, 0) + COALESCE(so.expensesValue, 0) + COALESCE(so.partsValue, 0)) - COALESCE(so.discountValue, 0)`
- **Base Líquida (NetBase)**: `(FaturamentoBruto * 0.88) - 3.50`
- **Comissão do Técnico**: `CASE WHEN (BaseLíquida > 0) THEN (BaseLíquida * 0.10) ELSE 0 END`

#### Métodos a serem atualizados:
1.  **`sumTotalValueCompleted`**: Incluir status `PAGO` e subtrair `discountValue`.
2.  **`sumTotalValueByMonthAndYear`**: Incluir status `PAGO` e subtrair `discountValue`.
3.  **`sumTotalValueCurrentMonth`**: Incluir status `PAGO` e subtrair `discountValue`.
4.  **`sumTotalTechnicianPaymentCompleted`**: Implementar a fórmula da Base Líquida e incluir status `PAGO`.
5.  **`sumTechnicianPaymentByMonthAndYear`**: Implementar a fórmula da Base Líquida e incluir status `PAGO`.
6.  **`sumTechnicianPaymentByStatus`**: Implementar a fórmula da Base Líquida (para o técnico).
7.  **`sumTechnicianPaymentByStatusAndMonthAndYear`**: Implementar a fórmula da Base Líquida.

### Alterações no Serviço: `DashboardService.java`
- Garantir que `totalProfit` seja calculado como `Faturamento Total - Comissões - Reembolsos`.
- Revisar a lógica de `inProgressOrders` para incluir variações de status se necessário.

## 2. Segurança e Integridade
- A lógica de transação deve ser mantida como `readOnly = true`.
- Os cálculos no banco de dados garantem que o usuário (mesmo Administrador) veja valores processados pelo motor de persistência, evitando discrepâncias entre o que o banco diz e o que o código Java processa.

## 3. Plano de Implementação
1. Modificar as queries no `ServiceOrderRepository.java`.
2. Verificar a consistência com `ServiceOrderService.java` (regras duplicadas, considerar extrair a fórmula para uma constante SQL se possível, ou documentar bem).
3. Testar com OS que possuam diversos componentes (peças, despesas, descontos) e status `PAGO`.
4. Validar se o Dashboard de Admin não "zera" mais ao mudar para `PAGO`.
