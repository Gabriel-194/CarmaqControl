package com.example.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// DTO de requisição para criar Ordem de Serviço
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderRequestDTO {

    @NotNull(message = "ID do cliente é obrigatório")
    private Long clientId;

    @NotNull(message = "ID da máquina é obrigatório")
    private Long machineId;

    @NotNull(message = "ID do técnico responsável é obrigatório")
    private Long technicianId;

    @NotNull(message = "Data do atendimento é obrigatória")
    private LocalDate serviceDate;

    @Size(max = 2000, message = "Descrição do problema não pode exceder 2000 caracteres")
    private String problemDescription;

    @Size(max = 2000, message = "Descrição do serviço não pode exceder 2000 caracteres")
    private String serviceDescription;

    @Size(max = 1000, message = "Observações não podem exceder 1000 caracteres")
    private String observations;

    // Tipo de serviço definido manualmente pelo proprietário
    @NotBlank(message = "Tipo de serviço é obrigatório")
    @Size(max = 200, message = "Tipo de serviço não pode exceder 200 caracteres")
    private String serviceType;

    // Valor do serviço (mão de obra) definido manualmente pelo proprietário
    @NotNull(message = "Valor do serviço é obrigatório")
    @Positive(message = "Valor do serviço deve ser positivo")
    private Double serviceValue;

    // Custo de deslocamento (opcional, pode ser preenchido depois)
    private Double travelCost;
}
