# Plano de Implementação - Fase 08: Performance e Escalabilidade

O objetivo desta fase é garantir que o sistema CarmarqControl continue rápido e eficiente à medida que o volume de Dados (Clientes, Ordens de Serviço, Despesas) cresce.

## Melhorias Propostas

### 1. Indexação de Banco de Dados
- Adicionar índices explícitos nas tabelas de relacionamento para acelerar as buscas por `service_order_id`.
- Tabelas afetadas: `service_expenses`, `service_parts`, `time_tracking`, `service_photos`.
- Adicionar índice no nome/razão social do cliente para otimizar o filtro de busca textual.

### 2. Otimização de Consultas JPA
- Garantir que não existam logs de "N+1 queries" (já verificado o uso de EntityGraphs em `ServiceOrder`, mas revisaremos as outras entidades).
- Adicionar `@BatchSize` em coleções se houver navegação bidirecional frequente.

### 3. Frontend: Otimização de Listas
- Verificar se o componente de listagem de ordens de serviço respeita a paginação do backend (já implementado no backend, validar consumo no React).

## Alterações no Código

#### [MODIFY] [ServiceExpense.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Models/ServiceExpense.java)
- Adicionar `@Index` para `service_order_id`.

#### [MODIFY] [ServicePart.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Models/ServicePart.java)
- Adicionar `@Index` para `service_order_id`.

#### [MODIFY] [TimeTracking.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Models/TimeTracking.java)
- Adicionar `@Index` para `service_order_id`.

#### [MODIFY] [ServicePhoto.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Models/ServicePhoto.java)
- Adicionar `@Index` para `service_order_id`.

#### [MODIFY] [Client.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Models/Client.java)
- Adicionar `@Index` para `company_name`.

## Plano de Verificação

### Testes de Infraestrutura
1. Verificar os logs do Hibernate para garantir o uso correto dos índices.
2. Monitorar o tempo de resposta do endpoint `/api/service-orders` com filtros aplicados.
