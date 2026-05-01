package com.example.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    // ID do técnico agora é verificado pelo backend (se for tecnico não é mandatório)
    private Long technicianId;

    @NotNull(message = "Data do atendimento é obrigatória")
    private LocalDate serviceDate;

    @Size(max = 2000, message = "Descrição do problema não pode exceder 2000 caracteres")
    private String problemDescription;

    @Size(max = 2000, message = "Descrição do serviço não pode exceder 2000 caracteres")
    private String serviceDescription;

    @Size(max = 1000, message = "Observações não podem exceder 1000 caracteres")
    private String observations;

    // Tipo de serviço restrito
    @NotBlank(message = "Tipo de serviço é obrigatório")
    @Size(max = 50, message = "Tipo de serviço excessivo")
    private String serviceType;
    
    // Novo Número de Chamado
    @NotBlank(message = "Número do chamado é obrigatório")
    private String numeroChamado;
    
    // Origem se for Manutencao
    private String manutencaoOrigin;



    // Valor do serviço (mão de obra) definido manualmente (Proprietário) ou calculado (Manutenção)
    private Double serviceValue;

    // Valor de desconto opcional na ordem de serviço
    private Double discountValue;

    // Novos campos de custo (Fase 6)
    private Double travelValue;
    private Double displacementValue;
    private Double reimbursementValue;

    // Campos detalhados de despesas (serão somados em reimbursementValue)
    private Double foodValue;
    private Double tollValue;
    private Double accommodationValue;
}
