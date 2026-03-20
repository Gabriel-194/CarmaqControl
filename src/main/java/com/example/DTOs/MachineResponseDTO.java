package com.example.DTOs;

import com.example.Domain.MachineTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO de resposta para máquina
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineResponseDTO {
    private Long id;
    private String name;
    private MachineTypeEnum machineType;
    private String model;
    private String serialNumber;
    private Double installationPrice;
    private String description;
    
    // Campos Específicos
    private String laserSize;
    private String laserKind;
    private Double laserPower;
    private String machineSize;
    private Double tonnage;
    private String command;
    private Double force;
    private Double diameter;
    private Integer rollerCount;
    
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
