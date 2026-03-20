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
    private java.time.LocalDate registeredDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;

    // Duração calculada em minutos (para exibição no frontend)
    private Long durationMinutes;
    private String durationFormatted; // Ex: "02:30"
    private String startTimeFormatted; // Ex: "14:30"
    private String endTimeFormatted; // Ex: "15:00"
}
