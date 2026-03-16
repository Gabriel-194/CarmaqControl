package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO de resposta para registro de tempo
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeTrackingResponseDTO {

    private Long id;
    private Long serviceOrderId;
    private String type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;

    // Duração calculada em minutos (para exibição no frontend)
    private Long durationMinutes;
}
