package com.example.Domain;

/**
 * Enum que define os tipos de máquinas suportados pelo sistema CarmarqControl.
 * Cada tipo possui campos técnicos específicos que são gerenciados no frontend e persistidos na entidade Machine.
 */
public enum MachineTypeEnum {
    LASER,
    DOBRADEIRA,
    GUILHOTINA,
    CURVADORA_TUBO,
    METALEIRA,
    CALANDRA,
    GRAVADORA_LASER
}
