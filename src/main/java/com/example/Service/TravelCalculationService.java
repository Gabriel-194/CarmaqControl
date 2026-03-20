package com.example.Service;

import org.springframework.stereotype.Service;

@Service
public class TravelCalculationService {

    private final Double headquartersLat = -25.5367932;
    private final Double headquartersLon = -49.3621944;
    private final Double costPerKm = 10.00;

    private static final double EARTH_RADIUS = 6371; // km
    private static final int AVERAGE_SPEED = 40; // km/h

    public Double calculateDistance(Double lat, Double lon) {
        if (lat == null || lon == null) return null;

        double dLat = Math.toRadians(lat - headquartersLat);
        double dLon = Math.toRadians(lon - headquartersLon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(headquartersLat)) * Math.cos(Math.toRadians(lat))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public Integer estimateMinutes(Double distance) {
        if (distance == null) return null;
        // Tempo = Distância / Velocidade * 60 (minutos)
        return (int) Math.round((distance / AVERAGE_SPEED) * 60);
    }

    public Double estimateCost(Double distance) {
        if (distance == null) return 0.0;
        return distance * costPerKm;
    }
}
