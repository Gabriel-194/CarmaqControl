package com.example.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO de requisição para registrar tempo de trabalho
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeTrackingRequestDTO {

    @NotBlank(message = "Tipo de registro é obrigatório")
    @Size(max = 30)
    private String type;

    @NotNull(message = "Data do apontamento é obrigatória")
    private java.time.LocalDate registeredDate;

    @NotNull(message = "Hora de início é obrigatória")
    private LocalDateTime startTime;

    // Pode ser nulo se o registro ainda está em andamento
    private LocalDateTime endTime;

    @Size(max = 500, message = "Descrição não pode exceder 500 caracteres")
    private String description;
}
