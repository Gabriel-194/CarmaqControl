package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeTrackingListDTO {
    private List<TimeTrackingResponseDTO> records;
    private Long totalMinutes;
    private String totalFormatted; // Ex: "02:30"
}
