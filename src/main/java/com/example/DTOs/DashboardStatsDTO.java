package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DTO para métricas do dashboard — retorna dados diferentes por role
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    // Métricas gerais
    private Long totalOrders;
    private Long openOrders;
    private Long inProgressOrders;
    private Long completedOrders;
    private Long cancelledOrders;
    private Long requiresInspectionOrders;
    private Long comProblemaOrders;
    private Long pendingApprovalPayments;

    // Métricas financeiras (somente para PROPRIETARIO e FINANCEIRO)
    private Double totalRevenue;
    private Double monthlyRevenue;
    private Double pendingPayments;
    private Double totalExpenses;            // Total de pagamentos aos técnicos
    private Double totalProfit;             // Lucro (Total Cobrado - Despesas)

    // Métricas financeiras do técnico (somente para TECNICO)
    private Double technicianEarnings;      // Total que o técnico já recebeu
    private Double technicianPendingPayment; // Total que o técnico tem a receber

    // Métricas de equipe (somente para PROPRIETARIO)
    private Long totalTechnicians;
    private Long totalClients;

    // Últimas atividades (lista de resumos de OS recentes)
    private List<RecentOrderDTO> recentOrders;

    // DTO interno para atividades recentes
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrderDTO {
        private Long id;
        private String clientName;
        private String machineName;
        private String status;
        private String technicianName;
        private Double totalValue;
        private String technicianPaymentStatus;
        private String openedAt;
    }
}
