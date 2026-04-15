# Especificação Técnica - Correção dos Filtros de Data das Ordens de Serviço (OS)

## 1. Arquitetura Backend

### Alterações no Repositório: `ServiceOrderRepository.java`
Modificar as consultas JPQL para utilizar `serviceDate` em vez de `openedAt` nos parâmetros de `month` e `year`.

#### Métodos a serem alterados:
- `findWithFilters`: Trocar `so.openedAt` por `so.serviceDate`.
- `findWithFiltersUnpaginated`: Trocar `so.openedAt` por `so.serviceDate`.
- `findWithFiltersTechnician`: Trocar `so.openedAt` por `so.serviceDate`.

### Exemplo de Alteração na Query:
```java
@Query("SELECT so FROM ServiceOrder so WHERE " +
       "(:status IS NULL OR so.status = :status) AND " +
       "(:year IS NULL OR (so.serviceDate IS NOT NULL AND EXTRACT(YEAR FROM so.serviceDate) = :year)) AND " +
       "(:month IS NULL OR (so.serviceDate IS NOT NULL AND EXTRACT(MONTH FROM so.serviceDate) = :month)) AND " +
       ...)
```

### Verificação de Consistência:
- Outras queries que utilizam `openedAt` para contagem (ex: `countByTechnicianAndYear`) também devem ser avaliadas. No entanto, para o Dashboard do Técnico, `openedAt` ou `serviceDate` podem ter sentidos diferentes. Mas para a listagem geral, a expectativa do usuário é `serviceDate`.

## 2. Arquitetura Frontend

### Validação na Página: `Ordens.jsx`
- O frontend já está enviando os parâmetros `month` e `year` corretamente.
- Garantir que o valor inicial do filtro de `year` (atualmente `new Date().getFullYear()`) seja adequado para exibir OS futuras. O componente de seleção no frontend já permite 2024 a 2028:
  ```javascript
  {[2024, 2025, 2026, 2027, 2028].map(y => (
      <option key={y} value={y}>{y}</option>
  ))}
  ```
- No entanto, a OS criada hoje para 2027 só aparecerá se o usuário mudar o filtro para 2027.

## 3. Plano de Implementação
1. Modificar `ServiceOrderRepository.java` (consultas `findWithFilters`, `findWithFiltersUnpaginated`, `findWithFiltersTechnician`).
2. Testar a filtragem criando uma OS com `serviceDate` no futuro e filtrando por esse ano.
3. Verificar a exportação de Excel para garantir que o filtro também foi aplicado corretamente.
