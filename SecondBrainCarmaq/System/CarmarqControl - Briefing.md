# CarmarqControl - Briefing

## Objetivo deste sistema

Este vault Obsidian implementa um **Second Brain + Sistema Multiagente de IA** para o projeto **CarmarqControl**.

O sistema foi definido para:

- armazenar conhecimento persistente;
- reduzir alucinação por meio de fontes de verdade explícitas;
- executar um pipeline de desenvolvimento com múltiplos agentes;
- manter tudo organizado em Markdown para Obsidian.

## Requisitos operacionais definidos

- Não fazer perguntas durante a execução.
- Executar e escrever todos os arquivos em disco.
- Manter todo o conteúdo em **pt-BR**.
- Usar Markdown compatível com Obsidian.
- Usar [[wikilinks]] quando fizer sentido.

## Estrutura-base solicitada

### Pastas principais

- `00 - Inbox/`
- `01 - Daily/`
- `02 - Projects/`
- `02 - Projects/Features/`
- `03 - Areas/`
- `03 - Areas/Desenvolvimento/`
- `04 - Knowledge/`
- `05 - Templates/`
- `AI/`
- `AI/Agents/`
- `System/`
- `Rules/`

## Componentes obrigatórios

### Template diário

Arquivo: [[05 - Templates/Template - Daily]]

### Briefing do sistema

Arquivo: [[System/CarmarqControl - Briefing]]

### Regras da IA

Arquivo: [[Rules/AI - Regras]]

### Orquestrador

Arquivo: [[AI/Orchestrator]]

### Agentes

- [[AI/Agents/planner]]
- [[AI/Agents/architect]]
- [[AI/Agents/security]]
- [[AI/Agents/backend]]
- [[AI/Agents/frontend]]
- [[AI/Agents/analyst]]
- [[AI/Agents/performance]]

### Visão geral do projeto

Arquivo: [[System/CarmarqControl - Visão Geral]]

## Direção funcional do vault

Este vault deve servir como base operacional para:

- captura rápida de informações;
- acompanhamento diário;
- planejamento e documentação de features;
- organização de conhecimento técnico e de produto;
- execução de um fluxo multiagente com rastreabilidade.

## Direção estrutural adicional

Além da estrutura acima, o sistema deve favorecer:

- clareza;
- baixa fricção;
- manutenção de longo prazo;
- crescimento sem sobre-engenharia;
- equilíbrio entre princípios inspirados em PARA e Zettelkasten, sem rigidez excessiva.

## Observação de implementação

Quando houver ausência de informação no vault, nas regras ou no código do projeto, a resposta padrão do sistema deve ser:

> Informação não encontrada no sistema.
