# PRD: Técnico Pode Criar OS & Despesas da OS

## Feature Overview
Este documento define as especificações para duas novas funcionalidades no sistema CarmarqControl:
1.  **Criação de Ordens de Serviço por Técnicos**: Permitir que técnicos cadastrem uma nova OS, garantindo que seja sempre atribuída a si mesmos, com controle de valores sugeridos em caso de instalação e obrigatoriedade do número de chamado.
2.  **Sistema de Despesas de OS**: Desativar o antigo campo `travelCost` e implementar um gerenciamento completo de despesas associadas a uma OS, com tipos específicos, cálculos automáticos (como km) e valores manuais (pedágio, alimentação, hospedagem), integrando o custo total à OS e ao repasse do profissional.

## User Roles
*   **PROPRIETARIO (Manager)**: Controle total do sistema. Revisa valores de instalação, lista despesas da OS mas com visão geral do financeiro.
*   **TECNICO (Employee)**: Registra suas próprias Ordens de Serviço. Lança despesas em OS durante a execução (status `EM_ANDAMENTO`).

## User Flows

### Técnico Pode Criar OS
1.  O **TECNICO** acessa o menu e clica em "Nova OS".
2.  Preenche os dados da nova OS. O campo de seleção de técnico não é exibido; o sistema assume o seu ID através do backend/token.
3.  Pode adicionar um novo cliente rapidamente durante a criação.
4.  É obrigatório fornecer o número do chamado.
5.  *Se Manutenção*: O valor da OS é estritamente via quantidade de horas, calculado de forma convencional.
6.  *Se Instalação*: O técnico fornece um "Valor Sugerido de Instalação", deixando claro no frontend que o número será validado pelo PROPRIETARIO na sequência.

### Gerenciamento de Despesas da OS (Substitui Deslocamento)
1.  O **TECNICO** acessa os Detalhes da OS que está com status `EM_ANDAMENTO`.
2.  Acessa uma nova aba "Despesas".
3.  Preenche o tipo de despesa (`DESLOCAMENTO_KM`, `PEDAGIO`, `ALIMENTACAO`, `HOSPEDAGEM`, `OUTRO`).
4.  *Se Deslocamento (Km)*: Fornece quantidade em Km. O valor pago em repasse é R$ 2,20/Km.
5.  *Se Outro*: Digita obrigatoriamente uma Descrição e o Valor.
6.  A despesa passa a integrar os custos da OS e substitui o local onde o `travelCost` costumava figurar.
7.  O valor em "Repasse Técnico" reflete 100% da devolução com Despesas + 10% da Mão de Obra.

## Functional Requirements
*   A rota `/api/service-orders` de `POST` (create) deve ser acessível por quem possuir role `PROPRIETARIO` ou `TECNICO`.
*   A OS deve inferir o ID do **técnico logado** se o Request for disparado pela role `TECNICO`. Se for `PROPRIETARIO`, requer a verificação explícita do DTO.
*   O sistema deve processar lançamentos de `ServiceExpense`, validados se a OS se encontrar em `EM_ANDAMENTO`.
*   O cálculo de Km do deslocamento (`quantityKm` * 2.20) deve ocorrer e persistir no servidor de forma síncrona, sendo que a front apenas previsualiza esse valor.
*   As despesas de OS devem ser somadas a uma propriedade cacheadas (`expensesValue`) na prória raiz de `ServiceOrder` e recalcular os totais caso uma despesa seja adicionada ou apagada.

## Non-Functional Requirements
*   Os formulários de Despesa da OS e criação da Nova OS devem transparecer e padronizar com o feedback do usuário (`Toaster`).
*   O backend Spring Boot deve utilizar transações adequadas para que totais de cache como `expensesValue` não fiquem inconsistentes.
*   Os novos scripts no React devem ser flexíveis tanto para permissões do PROPRIETARIO quanto para o do TECNICO.
