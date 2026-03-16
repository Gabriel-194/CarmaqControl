package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DTO com sugestões automáticas para criação de OS baseadas na máquina selecionada
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderSuggestionDTO {

    // Tipo de serviço sugerido
    private String suggestedServiceType;

    // Valor estimado do serviço (mão de obra)
    private Double estimatedServiceValue;

    // Horas estimadas de trabalho
    private Double estimatedHours;

    // Valor hora técnica
    private Double hourlyRate;

    // Informações da máquina
    private String machineType;
    private String machineModel;
    private String machineBrand;
    private String machineDescription;

    // Peças comuns sugeridas para este tipo de máquina (pode ser estendido)
    private List<String> suggestedParts;

    // Dica/observação automática
    private String autoObservation;
}
