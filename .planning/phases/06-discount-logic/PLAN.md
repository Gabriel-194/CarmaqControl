# Plano de Implementação - Fase 06: Lógica de Desconto no Pagamento

Esta fase foca em permitir que o usuário **Financeiro** ou **Proprietário** aplique um desconto final no momento de marcar a Ordem de Serviço como **PAGO**. 

## Alterações Propostas

### Backend (Spring Boot)

#### [MODIFY] [ServiceOrderService.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Service/ServiceOrderService.java)
- Alterar o método `approvePayment(Long orderId)` para `approvePayment(Long orderId, Double discountValue)`.
- O `discountValue` recebido deve atualizar `order.discountValue`.
- Garantir que a regra de comissão de 10% do técnico permaneça sobre o valor **antes** do desconto (Bruto), conforme prática comum, a menos que o usuário solicite o contrário (assumiremos bruto por padrão para não penalizar o técnico).

#### [MODIFY] [ServiceOrderController.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Controller/ServiceOrderController.java)
- Atualizar o endpoint `PUT /{id}/approve-payment` para aceitar um corpo contendo `discountValue`.

### Frontend (React)

#### [MODIFY] [OrdemDetalhes.jsx](file:///c:/Users/gabri/Downloads/CarmaqControl/carmarq-control-frontend/src/pages/OrdemDetalhes.jsx)
- Adicionar um pequeno input de desconto ou um modal de confirmação que peça o valor do desconto ao clicar em "Aprovar Pagamento".
- No momento, existe um alerta genérico. Substituiremos por um fluxo que permita confirmar o valor final.

## Plano de Verificação

### Testes Manuais
1. **Fluxo Financeiro**: Abrir uma OS `CONCLUIDA`.
2. Clicar em **Aprovar Pagamento**.
3. Informar um desconto (ex: R$ 50,00). 
4. Confirmar a operação.
5. Verificar se:
   - O status da OS mudou para `PAGO`.
   - O `Total Faturado` refletiu o desconto.
   - O `Repasse do Técnico` permaneceu correto (baseado no bruto inicial).
