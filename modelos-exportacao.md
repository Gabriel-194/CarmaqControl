# Modelos Oficiais de Exportação (Referências para a Fase 2)

## 1. Modelo: Ordem de Serviço em Garantia / Manutenção (PDF)
**Cabeçalho da Empresa:**
- CARMAQ MÁQUINAS INDUSTRIAIS / CARMAQ SERVICE
- CNPJ: 60.526.327/0001-23 | Av. Das Araucárias, 4255 | 83707-065 | Araucária | Paraná
- Fone: 55 41 3346 1430 | 55 41 99663 1349
- Email: vendas@carmaq.ind.br | service@carmaq.ind.br

**Dados do Cliente e OS:**
- Título: ORDEM DE SERVIÇO EM GARANTIA
- Cliente (Ex: VALENTIN REP.E COMERCIO LTDA), Endereço, CNPJ, Contato, IE, Email.
- Tipo: MANUTENÇAO - MAQUINA - CLIENTE

**Tabela de Serviços (Colunas):**
1. Item | 2. Unid. (MO, KM, DESP) | 3. Qtde. | 4. Código | 5. Descrição | 6. R$ Unitario | 7. R$ Total
*Exemplos de linhas a injetar:* Hora Deslocamento (85,00), Hora Trabalhada (185,00), Quilometro rodado ida e volta (2.20), REFEIÇÃO.

---

## 2. Modelo: Entrega Técnica / Instalação (Excel)
**Cabeçalho e Dados:**
- Título: ORDEM DE SERVIÇO (Ex: OS2026MMDD00)
- Data: 00/00/2026
- Cliente, Endereço, Cidade, Estado, CNPJ, IE, Contato, Email, Fone
- Tipo: ENTREGA TECNICA - MAQUINA - CLIENTE

**Tabela de Serviços (Colunas idênticas):**
1. Item | 2. Unid. | 3. Qtde. | 4. Código | 5. Descrição | 6. R$ Unitario | 7. R$ Total

---

## 3. Modelo: Relatório de Despesas (Excel)
**Cabeçalho:**
- Título: RELATÓRIO DE DESPESAS
- Cliente, OS, Efetuado por, DATA, Veículo, Placa, Cidade

**Tabela de Despesas (Colunas):**
1. Descrição | 2. Qtde. | 3. Valor
*Linhas fixas:* Refeição, Hotel, Passagem Aérea, Taxi, Pedágio, Combustível, Estacionamento, Aluguel Carro, Quilometragem, Desp. com Material, Outros (especificar).

**Rodapé:**
- TOTAL Despesas, Valor à Creditar, Outras Informações.
- Campos para Assinatura do técnico, Assinatura do responsável e Datas.