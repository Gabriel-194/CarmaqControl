# Requirements: Fase 5 - Evolução do Sistema de OS

## 🐞 1. Bugfixes
- **Fluxo de Aprovação de Pagamento:** Corrigir a lógica que impede a aprovação correta dos pagamentos.
- **Ajuste de Valores:** Revisar e corrigir falhas no cálculo da rotina de ajuste de valores.

## ⚙️ 2. Features e Melhorias de Interface
- **Detalhamento de OS (Owner/Admin/Financial):** 
    - Exibir valor total da OS.
    - Exibir destrinchamento de despesas, peças e fotos.
    - Exibir valor exato do comissionamento do técnico.
- **Remoção de Cronômetro:** 
    - Remover funcionalidade de cronometragem automática.
    - Tornar o lançamento de horas (serviço e deslocamento) 100% manual.

## 🚦 3. Regras de Negócio e Status
- **Cálculo dos 10% do Técnico:** 
    - Base de cálculo: Valor Total da OS (Trabalho + Deslocamento + Despesas).
    - Automação do cálculo de repasse.
- **Status "Pago":** 
    - Adicionar status `PAGO`.
    - Gatilho: Autorização pelo setor Financeiro.
- **Gestão de Peças e Despesas:** 
    - Funcionalidades de Editar e Excluir.
    - Restrição: Bloqueado se status for `PAGO`.
- **Gestão de Fotos:** 
    - Funcionalidade de Gerenciamento/Edição.
    - Restrição: Bloqueado se status for `CONCLUIDO`.

## 🔒 4. Segurança
- **Limitação de Payload:** Restringir o tamanho dos dados enviados nas requisições de edição (peças, despesas e fotos) para evitar abusos.
