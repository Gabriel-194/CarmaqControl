# Regras da IA

## Objetivo

Estas regras definem como os agentes e documentos do sistema CarmarqControl devem operar dentro deste vault.

## Fonte da Verdade

As únicas fontes autorizadas para definição de comportamento, contexto e decisão são:

- [[System/CarmarqControl - Briefing]]
- [[System/CarmarqControl - Visão Geral]]
- pasta `System/`
- pasta `AI/`
- código-fonte do projeto

## Anti-alucinação

- Nunca inventar regra, requisito, fluxo ou permissão.
- Nunca assumir comportamento de tela, API, banco ou usuário sem evidência.
- Nunca preencher lacunas com "provavelmente", "deve ser" ou inferências não verificadas.
- Sempre priorizar fatos documentados no sistema e no código.
- Se faltar contexto suficiente, responder exatamente:
  `Informação não encontrada no sistema`

## Consistência

- Seguir sempre o briefing antes de gerar qualquer saída.
- Respeitar os papéis do negócio: `Owner`, `Technician` e `Financial`.
- Manter alinhamento entre PRD, SPEC, segurança, implementação e validação.
- Não contradizer artefatos anteriores sem registrar a mudança explicitamente.

## Escrita

- Escrever sempre em pt-BR.
- Usar linguagem técnica, objetiva e clara.
- Preferir títulos previsíveis e estrutura consistente.
- Usar [[wikilinks]] quando houver referência interna útil.

## Fluxo multiagente

- O fluxo padrão deve seguir o arquivo [[AI/Orchestrator]].
- Nenhum agente deve pular sua entrada formal.
- Toda saída deve ser aproveitada pela próxima etapa do pipeline.
- Toda feature deve ter documentação rastreável em `02 - Projects/Features/`.

## Persistência do conhecimento

- Decisões relevantes devem ser salvas no vault.
- Aprendizados técnicos devem ir para `04 - Knowledge/`.
- Trabalho em andamento deve ser rastreado em `02 - Projects/` ou `03 - Areas/Desenvolvimento/`.

## Critério de qualidade

- Clareza antes de volume.
- Estrutura antes de improviso.
- Evidência antes de conclusão.
