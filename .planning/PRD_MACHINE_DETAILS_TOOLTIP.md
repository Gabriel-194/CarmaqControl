# PRD - Detalhes da Máquina (Tooltip Móvel)

## 1. Visão Geral
O objetivo deste recurso é melhorar a experiência do usuário na "Biblioteca de Máquinas" (Machine Library), permitindo que os usuários visualizem rapidamente todas as especificações técnicas de uma máquina sem precisar abrir o modal de Edição. Isso será alcançado através de um "mini-modal" (tooltip flutuante) que aparece ao passar o mouse e segue o cursor do mouse.

## 2. Fluxos do Usuário
1. **Passar o Mouse**: O usuário navega até a página "Biblioteca de Máquinas" e move o mouse sobre uma linha de máquina na tabela.
2. **Exibição**: Um tooltip flutuante aparece perto do cursor do mouse, mostrando os detalhes completos da máquina (Modelo, Número de Série, Tipo, Preço de Instalação e todas as especificações técnicas como Potência do Laser, Tonelagem, etc.).
3. **Movimento**: Conforme o usuário move o mouse dentro da linha, o tooltip segue o cursor com um leve deslocamento (offset) para evitar obscurecer o ponto de interesse.
4. **Ocultação**: Quando o mouse sai da linha, o tooltip desaparece.

## 3. Requisitos Funcionais
- **Conteúdo Dinâmico**: O tooltip deve mostrar campos diferentes baseados no `machineType` (Ex: `laserPower` para lasers, `tonnage` para dobradeiras).
- **Posicionamento**: O tooltip deve seguir o cursor do mouse dinamicamente.
- **Responsividade**: O tooltip deve permanecer dentro dos limites da viewport (não transbordar da tela).
- **Estética**: O design deve seguir a identidade visual da "CarmarqControl" (Tema Verde/Branco, limpo e profissional).
- **Performance**: O rastreamento do mouse deve ser suave e não causar atrasos na interface do usuário (lag).

## 4. Requisitos Não Funcionais
- **Performance**: A detecção de hover e renderização do tooltip devem ser eficientes.
- **Acessibilidade**: O tooltip não deve interferir com leitores de tela (ARIA adequado, se aplicável).
- **Manutenibilidade**: A lógica dos campos das máquinas deve, idealmente, reutilizar os mapeamentos já definidos no `MachineModal.jsx`.

## 5. Expectativas de Design UI/UX
- **Estilo**: Sombras suaves, cantos arredondados, fundo limpo.
- **Paleta de Cores**: 
  - Fundo: Branco ou cinza claro (`#f8f9fa`)
  - Borda: Verde (`#28a745` ou verde primário da empresa)
  - Texto: Cinza escuro para rótulos, verde profundo para valores.
- **Animação**: Transição sutil de fade-in/out.

## 6. Expectativas de Lógica de Negócios
- Exibir apenas campos que não sejam nulos e sejam relevantes para o tipo de máquina.
- Garantir que o "Preço de Instalação" esteja formatado como moeda (BRL).
- Usar rótulos em Português para todos os campos.
