package com.example.Controller;

import com.example.DTOs.CardBreakdownDTO;
import com.example.DTOs.DashboardStatsDTO;
import com.example.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Controller para métricas do dashboard — disponível para todos os roles autenticados
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<DashboardStatsDTO> getStats(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year) {
        return ResponseEntity.ok(dashboardService.getStats(month, year));
    }

    // Detalhamento financeiro por OS para um card específico do dashboard
    @GetMapping("/card-breakdown")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<CardBreakdownDTO> getCardBreakdown(
            @RequestParam("card") String card,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year) {
        return ResponseEntity.ok(dashboardService.getCardBreakdown(card, month, year));
    }
}
