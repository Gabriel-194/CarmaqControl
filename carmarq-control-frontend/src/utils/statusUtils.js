export const statusMap = {
    ABERTA:          { label: 'Aberta',          css: 'status-aberto' },
    EM_ANDAMENTO:    { label: 'Em Andamento',     css: 'status-em-andamento' },
    CONCLUIDA:       { label: 'Concluída',        css: 'status-concluido' },
    CANCELADA:       { label: 'Cancelada',        css: 'status-cancelada' },
    COM_PROBLEMA:    { label: 'Com Problema',     css: 'status-cancelada' },
    REQUER_INSPECAO: { label: 'Requer Inspeção',  css: 'status-inspecao' },
    PAGO:            { label: 'Pago',             css: 'status-concluido' },
    REJEITADA:       { label: 'Rejeitada',        css: 'status-laranja' },
    EM_REVISAO:      { label: 'Em Revisão',       css: 'status-laranja' },
}

export const serviceTypeLabels = {
    MANUTENCAO_GARANTIA: 'Manutenção — Garantia Valentim',
    MANUTENCAO_CARMAQ:   'Manutenção — Carmaq',
    MANUTENCAO:          'Manutenção',
    INSTALACAO:          'Instalação',
}

export const manutencaoOriginLabels = {
    VALENTIM: 'Garantia Valentim (R$ 185/h)',
    CARMARQ:  'Carmaq (R$ 250/h)',
}

export const paymentStatusMap = {
    A_RECEBER:          { label: 'Em andamento',          color: '#6b7280' },
    PENDENTE_APROVACAO: { label: 'Aguardando aprovação',  color: '#f59e0b' },
    APROVADO:           { label: 'Aprovado (aguardando confirmação)', color: '#10b981' },
    REJEITADO:          { label: 'Rejeitado',             color: '#ef4444' },
    RECEBIDO:           { label: 'Recebido',              color: '#059669' },
}

export const expenseTypeLabels = {
    DESLOCAMENTO_KM: 'Deslocamento (ida e volta)',
    PEDAGIO:         'Pedágio',
    ALIMENTACAO:     'Alimentação',
    HOSPEDAGEM:      'Hospedagem',
    PASSAGEM_AEREA:  'Passagem Aérea',
    TAXI:            'Táxi',
    COMBUSTIVEL:     'Combustível',
    ESTACIONAMENTO:  'Estacionamento',
    ALUGUEL_CARRO:   'Aluguel de Carro',
    MATERIAL:        'Material',
    OUTRO:           'Outro',
}
