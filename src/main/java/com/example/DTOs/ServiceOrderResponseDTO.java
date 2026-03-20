package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// DTO de resposta para Ordem de Serviço — campos financeiros são filtrados por role no Service
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOrderResponseDTO {

    private Long id;

    // Dados do cliente
    private Long clientId;
    private String clientName;
    private String clientAddress;

    // Dados da máquina
    private Long machineId;
    private String machineName;
    private String machineType;

    // Dados do técnico
    private Long technicianId;
    private String technicianName;

    // Status e data de atendimento
    private String status;
    private LocalDate serviceDate;

    // Descrições
    private String problemDescription;
    private String serviceDescription;
    private String observations;

    // Tipo de serviço restrito
    private String serviceType;
    
    // Novo Número de Chamado
    private String numeroChamado;
    
    // Origem da Manutenção
    private String manutencaoOrigin;

    // Km rodados para cobrança de deslocamento
    private Double displacementKm;

    // Valores financeiros (podem ser null ou zero para TECNICO)
    private Double serviceValue;
    private Double partsValue;
    private Double expensesValue;
    private Double totalValue;

    // Valor visível ao técnico
    private Double technicianPayment;

    // Status do pagamento do técnico: A_RECEBER ou RECEBIDO
    private String technicianPaymentStatus;

    // Lucro Líquido (Total - Pagamento Técnico)
    private Double netProfit;

    // Datas
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

    // Campos logísticos (calculados dinamicamente no mapeamento)
    private Double distanceKm;
    private Integer estimatedMinutes;
    private Double estimatedTravelCost;
}
