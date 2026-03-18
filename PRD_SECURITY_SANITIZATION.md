# PRD - Sanitização de Dados e Validação Centralizada

## Feature Overview
Garantir que o sistema CarmarqControl siga o princípio de "Frontend Burro / Backend Inteligente" e que nenhuma informação sensível seja vazada nas respostas da API.

## Funcionalidades e Requisitos

### 1. Validação Centralizada no Backend
- Toda a lógica de validação (tamanho de campo, obrigatoriedade, regras de negócio) deve residir no Backend.
- O Frontend não deve replicar regras de negócio, apenas exibir erros retornados pela API.

### 2. Sanitização de DTOs de Resposta
- **Senhas**: Nunca devem constar em nenhum DTO de saída, mesmo criptografadas.
- **IDs Internos**: IDs técnicos que não são necessários para o frontend devem ser omitidos.
- **Dados Sensíveis**: Campos como `failedLoginAttempts`, `accountLockedUntil` ou segredos internos não devem ser serializados para o cliente.

### 3. Remoção de Lógica do Frontend
- O frontend deve ser apenas uma camada de apresentação.
- Regras como "Se o status é X, então valor é Y" devem vir pré-calculadas do backend.

## User Roles
Atinge todos os usuários: PROPRIETARIO, FINANCEIRO e TECNICO.

## Non-Functional Requirements
- **Security**: Prevenção de Information Exposure (CWE-200).
- **Maintainability**: Único ponto de verdade para validações (DRY - Don't Repeat Yourself).
