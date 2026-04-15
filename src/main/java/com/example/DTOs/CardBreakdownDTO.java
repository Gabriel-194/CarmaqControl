package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DTO para detalhamento financeiro dos cards do dashboard
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardBreakdownDTO {

    private String cardType; // Tipo do card solicitado
    private Double total;    // Valor total do card
    private List<OrderLineDTO> orders; // Detalhamento por OS

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLineDTO {
        private Long id;
        private String osCode;
        private String clientName;
        private String technicianName;
        private Double value; // Valor que esta OS contribui para o card
    }
}
