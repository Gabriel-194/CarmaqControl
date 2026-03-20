package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelEstimateDTO {
    private Double distanceKm;
    private Integer estimatedMinutes;
    private Double estimatedCost;
}
