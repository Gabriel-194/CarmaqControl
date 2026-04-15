# PRD - Correção dos Filtros de Data das Ordens de Serviço (OS)

## 1. Visão Geral
Atualmente, os filtros de mês e ano na listagem de Ordens de Serviço (OS) utilizam o campo `openedAt` (data de abertura do registro no banco) para realizar a filtragem. No entanto, os usuários esperam que o filtro utilize a `serviceDate` (data programada para o atendimento), pois uma OS pode ser criada hoje para um atendimento no mês que vem ou no ano que vem.

## 2. Fluxos do Usuário
1. **Criação**: O usuário cria uma nova OS e define a "Data do Atendimento" (`serviceDate`) para uma data futura (ex: Janeiro de 2027).
2. **Filtragem**: O usuário acessa a tela de listagem de Ordens de Serviço.
3. **Seleção**: O usuário seleciona o Ano "2027" e o Mês "Janeiro".
4. **Resultado Esperado**: A OS recém-criada deve aparecer na lista.
5. **Resultado Atual**: A lista fica vazia (ou não mostra a OS) porque o sistema filtra pelo ano atual (ano de abertura da OS).

## 3. Requisitos Funcionais
- Alterar a lógica de filtragem no backend para utilizar o campo `serviceDate` em vez de `openedAt`.
- Garantir que a filtragem funcione corretamente para todos os papéis (Proprietário, Financeiro, Técnico).
- Garantir que a exportação para Excel também utilize o novo critério de filtragem.
- Verificar se outros locais do sistema (dashboard, relatórios) também precisam dessa alteração para manter a consistência.

## 4. Requisitos Não Funcionais
- **Performance**: A consulta ao banco de dados deve continuar sendo eficiente (verificar índices).
- **Consistência**: O comportamento deve ser uniforme em toda a plataforma.

## 5. Expectativas de Lógica de Negócios
- Filtro por **Ano**: Deve retornar todas as OS onde o ano de `serviceDate` corresponde ao fornecido.
- Filtro por **Mês**: Deve retornar todas as OS onde o mês de `serviceDate` corresponde ao fornecido (se o mês for informado).
- Se `year` for fornecido mas `month` for nulo, filtrar apenas pelo ano.
