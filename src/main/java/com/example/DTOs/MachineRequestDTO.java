package com.example.DTOs;

import com.example.Domain.MachineTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de requisição para criar/atualizar máquina
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineRequestDTO {

    @NotBlank(message = "Nome da máquina é obrigatório")
    @Size(max = 150, message = "Nome não pode exceder 150 caracteres")
    private String name;

    @NotNull(message = "Tipo da máquina é obrigatório")
    private MachineTypeEnum machineType;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 150, message = "Modelo não pode exceder 150 caracteres")
    private String model;

    @NotBlank(message = "Número de série é obrigatório")
    @Size(max = 100, message = "Número de série não pode exceder 100 caracteres")
    private String serialNumber;

    @NotNull(message = "Preço de instalação é obrigatório")
    private Double installationPrice;

    @Size(max = 500, message = "Descrição não pode exceder 500 caracteres")
    private String description;

    // --- CAMPOS ESPECÍFICOS (Opcionais no DTO) ---
    private String laserSize;
    private String laserKind;
    private Double laserPower;
    private String machineSize;
    private Double tonnage;
    private String command;
    private Double force;
    private Double diameter;
    private Integer rollerCount;
}
