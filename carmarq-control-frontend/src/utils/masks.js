// Funções utilitárias de máscara para inputs do sistema

/**
 * Aplica máscara de telefone: (00) 0000-0000 ou (00) 00000-0000
 * Suporta telefones fixos (8 dígitos) e celulares (9 dígitos).
 */
export function maskPhone(value) {
    const digits = value.replace(/\D/g, '').slice(0, 11)
    if (digits.length === 0) return ''
    if (digits.length <= 10) {
        // Telefone fixo: (00) 0000-0000
        return digits
            .replace(/^(\d{2})(\d)/, '($1) $2')
            .replace(/(\d{4})(\d)/, '$1-$2')
    }
    // Celular: (00) 00000-0000
    return digits
        .replace(/^(\d{2})(\d)/, '($1) $2')
        .replace(/(\d{5})(\d)/, '$1-$2')
}

/**
 * Aplica máscara de CPF: 000.000.000-00
 */
export function maskCPF(value) {
    const digits = value.replace(/\D/g, '').slice(0, 11)
    return digits
        .replace(/^(\d{3})(\d)/, '$1.$2')
        .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
        .replace(/\.(\d{3})(\d)/, '.$1-$2')
}

/**
 * Aplica máscara de CNPJ: 00.000.000/0000-00
 */
export function maskCNPJ(value) {
    const digits = value.replace(/\D/g, '').slice(0, 14)
    return digits
        .replace(/^(\d{2})(\d)/, '$1.$2')
        .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
        .replace(/\.(\d{3})(\d)/, '.$1/$2')
        .replace(/(\d{4})(\d)/, '$1-$2')
}

/**
 * Aplica máscara de CEP: 00000-000
 */
export function maskCEP(value) {
    const digits = value.replace(/\D/g, '').slice(0, 8)
    return digits.replace(/^(\d{5})(\d)/, '$1-$2')
}

/**
 * Remove todos os caracteres não numéricos (útil para envio ao backend)
 */
export function onlyDigits(value) {
    return value.replace(/\D/g, '')
}
