package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de resposta para máquina
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineResponseDTO {

    private Long id;
    private String machineType;
    private String model;
    private String brand;
    private String description;
    private Double hourlyRate;
    private Double estimatedHours;
    private Boolean active;
}
