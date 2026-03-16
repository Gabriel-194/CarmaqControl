package com.example.DTOs;

import jakarta.validation.constraints.Min;
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

    @NotBlank(message = "Tipo da máquina é obrigatório")
    @Size(max = 100, message = "Tipo da máquina não pode exceder 100 caracteres")
    private String machineType;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 150, message = "Modelo não pode exceder 150 caracteres")
    private String model;

    @Size(max = 100, message = "Marca não pode exceder 100 caracteres")
    private String brand;

    @Size(max = 500, message = "Descrição não pode exceder 500 caracteres")
    private String description;

    @NotNull(message = "Valor da hora técnica é obrigatório")
    @Min(value = 0, message = "Valor da hora técnica deve ser positivo")
    private Double hourlyRate;

    @NotNull(message = "Estimativa de horas é obrigatória")
    @Min(value = 0, message = "Estimativa de horas deve ser positiva")
    private Double estimatedHours;
}
