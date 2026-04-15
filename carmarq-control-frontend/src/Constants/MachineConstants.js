export const typeLabels = {
    LASER_CHAPA: 'Laser Chapa',
    LASER_TUBO: 'Laser Tubo',
    DOBRADEIRA: 'Dobradeira',
    GUILHOTINA: 'Guilhotina',
    CURVADORA_TUBO: 'Curvadora de Tubo',
    METALEIRA: 'Metaleira',
    CALANDRA: 'Calandra',
    GRAVADORA_LASER: 'Gravadora a Laser',
}

export const fieldLabels = {
    laserSize: 'Tamanho da Mesa',
    laserKind: 'Tipo (Fechada / Aberta)',
    laserPower: 'Potência (W)',
    machineSize: 'Tamanho',
    tonnage: 'Tonelagem',
    command: 'Comando',
    force: 'Força',
    diameter: 'Diâmetro máximo (mm)',
    rollerCount: 'Quantidade de Rolos',
}

export const fieldsByType = {
    LASER_CHAPA: ['laserSize', 'laserKind', 'laserPower'],
    LASER_TUBO: ['laserSize', 'laserKind', 'laserPower'],
    DOBRADEIRA: ['machineSize', 'tonnage', 'command'],
    GUILHOTINA: ['machineSize', 'tonnage', 'command'],
    CURVADORA_TUBO: ['machineSize', 'command', 'force', 'diameter'],
    METALEIRA: ['machineSize', 'tonnage'],
    CALANDRA: ['machineSize', 'command', 'force', 'diameter', 'rollerCount'],
    GRAVADORA_LASER: ['machineSize', 'laserPower'],
}
