package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceExpenseResponseDTO {
    private Long id;
    private Long serviceOrderId;
    private String expenseType;
    private Double quantity;
    private Double value;
    private String description;
    private String expenseTypeLabel; // label em português
}
