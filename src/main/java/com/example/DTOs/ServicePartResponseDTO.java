package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de resposta para peça de serviço
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePartResponseDTO {

    private Long id;
    private Long serviceOrderId;
    private String partName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice; // quantidade * preço unitário (calculado)
}
