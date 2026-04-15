# Especificação Técnica - Tooltip de Detalhes da Máquina

## 1. Arquitetura Frontend

### Novo Componente: `MachineTooltip.jsx`
- **Responsabilidade**: Renderizar o tooltip flutuante com as informações da máquina.
- **Entradas (Props)**: `machine`, `position` ( { x, y } ).
- **Comportamento**: 
  - Renderizar apenas se `machine` não for nulo.
  - Usar CSS `position: fixed` com `top` e `left` baseados na prop `position`.
  - Aplicar offset de `15px` para evitar que o mouse cubra o conteúdo.
  - Verificar se o tooltip ultrapassa a tela e ajustar a posição se necessário.

### Atualização da Página: `Machines.jsx`
- **Novo Estado**: 
  - `hoveredMachine`: Mantém o objeto da máquina que está sob o mouse.
  - `mousePos`: Armazena `{ x, y }` atuais do mouse.
- **Eventos**:
  - `onMouseEnter` / `onMouseLeave` nas linhas (`<tr>`) da tabela.
  - `onMouseMove` na tabela ou linhas para atualizar a posição.
- **Integração**: Renderizar o `MachineTooltip` no final do componente.

## 2. Modelos de Dados
O componente consumirá o objeto `machine` retornado pelo backend. 

### Campos a Exibir (Comuns):
- **Modelo**: `model`
- **S/N**: `serialNumber`
- **Tipo**: `machineType` (formatado com `typeLabels`)
- **Preço de Instalação**: `installationPrice` (formatado como moeda)

### Campos Técnicos (Condicionais):
Baseado no `machineType`, exibir conforme mapeado em `MachineModal.jsx`:
- `laserPower`, `laserSize`, `tonnage`, `command`, `force`, `diameter`, `rollerCount`.

## 3. Estilo e CSS
- Novo arquivo: `carmarq-control-frontend/src/Styles/MachineTooltip.css`
- **Estilos**:
  - `.machine-tooltip`: Fundo branco, borda `2px solid var(--primary-color)`, sombra suave, z-index alto (1000+).
  - `.tooltip-field`: Layout de linha com "Label" em negrito e "Valor" em destaque.
  - `.tooltip-title`: Título formatado com o Modelo da máquina.

## 4. Segurança
- Como o componente renderiza dados que já foram sanitizados pelo backend e frontend, não há riscos adicionais de segurança.
- Os dados exibidos já estão disponíveis para o usuário no modal de edição.

## 5. Plano de Implementação
1. Criar o arquivo de estilos `MachineTooltip.css`.
2. Criar o componente funcional `MachineTooltip.jsx`.
3. Adicionar lógica de hover e mouse tracking em `Machines.jsx`.
4. Testar a responsividade e o posicionamento.
