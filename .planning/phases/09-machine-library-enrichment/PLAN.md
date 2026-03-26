# Plano de Implementação - Fase 09: Enriquecimento da Biblioteca de Máquinas

Esta fase visa transformar a Biblioteca de Máquinas em uma ferramenta de automação ativa, permitindo que o sistema sugira valores de serviço baseados na ficha técnica do equipamento.

## Melhorias Propostas

### 1. Expansão do Modelo de Dados
- **Novos Campos**: Adicionar `fabricante`, `anoFabricacao` e `valorHoraSugerido` à entidade `Machine.java`.
- **Automação Financeira**: O campo `valorHoraSugerido` será utilizado como base para orçamentos automáticos em novas OS.

### 2. Lógica de Sugestão Automática
- **ServiceOrderService**: Atualizar o método `generateSuggestions` para que, ao selecionar uma máquina, o `serviceValue` (Mão de Obra) seja pré-preenchido com o `valorHoraSugerido` daquela máquina (multiplicado por uma estimativa padrão de horas se necessário).

### 3. Melhorias na Interface (Frontend)
- **Cadastro de Máquinas**: Atualizar a página `Machines.jsx` para incluir os novos campos no formulário de criação/edição.
- **Criação de OS**: Garantir que, ao selecionar a máquina em `NovaOS.jsx`, o valor sugerido seja injetado no formulário se for uma manutenção.

## Alterações no Código

#### [MODIFY] [Machine.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Models/Machine.java)
- Adicionar os campos: `private String fabricante`, `private Integer anoFabricacao`, `private Double valorHoraSugerido`.

#### [MODIFY] [ServiceOrderService.java](file:///c:/Users/gabri/Downloads/CarmaqControl/src/main/java/com/example/Service/ServiceOrderService.java)
- Atualizar a lógica de `generateSuggestions` (ou o endpoint de sugestão) para buscar o valor da máquina.

#### [MODIFY] [Machines.jsx](file:///c:/Users/gabri/Downloads/CarmaqControl/carmarq-control-frontend/src/pages/Machines.jsx)
- Atualizar o modal de cadastro de máquinas.

## Plano de Verificação

### Testes Manuais
1. **Cadastro**: Cadastrar uma máquina com Fabricante: "Yanmar", Ano: 2023, Valor Sugerido: R$ 250,00.
2. **Nova OS**: Criar uma OS, selecionar essa máquina e verificar se o valor sugerido de R$ 250,00 aparece automaticamente no campo de Mão de Obra.
3. **Consistência**: Garantir que máquinas antigas continuem funcionando (campos nulos).
