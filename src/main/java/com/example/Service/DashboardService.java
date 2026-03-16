package com.example.Service;

import com.example.DTOs.DashboardStatsDTO;
import com.example.Models.ServiceOrder;
import com.example.Models.Usuario;
import com.example.Repository.ServiceOrderRepository;
import com.example.Repository.UsuarioRepository;
import com.example.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

// Serviço para métricas do dashboard — retorna dados diferentes conforme a role
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClientRepository clientRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        String role = currentUser.getRole();

        DashboardStatsDTO.DashboardStatsDTOBuilder builder = DashboardStatsDTO.builder();

        if ("TECNICO".equals(role)) {
            // Técnico vê apenas suas OS
            List<ServiceOrder> myOrders = serviceOrderRepository.findByTechnicianId(currentUser.getId());
            builder.totalOrders((long) myOrders.size())
                    .openOrders(myOrders.stream().filter(o -> "ABERTA".equals(o.getStatus())).count())
                    .inProgressOrders(myOrders.stream().filter(o -> "EM_ANDAMENTO".equals(o.getStatus())).count())
                    .completedOrders(myOrders.stream().filter(o -> "CONCLUIDA".equals(o.getStatus())).count());

            // Monta lista de OS recentes do técnico
            List<DashboardStatsDTO.RecentOrderDTO> recent = myOrders.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(5)
                    .map(o -> mapToRecentDTO(o, role))
                    .collect(Collectors.toList());
            builder.recentOrders(recent);

            // Métricas financeiras pessoais do técnico
            builder.technicianPendingPayment(
                    serviceOrderRepository.sumTechnicianPaymentByStatus(currentUser.getId(), "A_RECEBER"));
            builder.technicianEarnings(
                    serviceOrderRepository.sumTechnicianPaymentByStatus(currentUser.getId(), "RECEBIDO"));

        } else {
            // PROPRIETARIO e FINANCEIRO veem métricas globais
            builder.totalOrders(serviceOrderRepository.count())
                    .openOrders(serviceOrderRepository.countByStatus("ABERTA"))
                    .inProgressOrders(serviceOrderRepository.countByStatus("EM_ANDAMENTO"))
                    .completedOrders(serviceOrderRepository.countByStatus("CONCLUIDA"))
                    .cancelledOrders(serviceOrderRepository.countByStatus("CANCELADA"))
                    .requiresInspectionOrders(serviceOrderRepository.countByStatus("REQUER_INSPECAO"));

            // Métricas financeiras
            builder.totalRevenue(serviceOrderRepository.sumTotalValueCompleted());
            builder.monthlyRevenue(serviceOrderRepository.sumTotalValueCurrentMonth());
            builder.pendingPayments(serviceOrderRepository.sumTotalValuePending());
            
            // Lucro = Faturamento Total - Pagamentos de Técnicos
            Double revenue = builder.build().getTotalRevenue() != null ? builder.build().getTotalRevenue() : 0.0;
            Double totalTechPayments = serviceOrderRepository.findByStatus("CONCLUIDA").stream()
                    .mapToDouble(os -> os.getTechnicianPayment() != null ? os.getTechnicianPayment() : 0.0)
                    .sum();
            builder.totalProfit(revenue - totalTechPayments); 
            
            if ("PROPRIETARIO".equals(role)) {
                // Proprietário também vê métricas de equipe
                builder.totalTechnicians(usuarioRepository.countByRole("TECNICO"))
                        .totalClients(clientRepository.count());
            }

            // OS recentes globais
            List<DashboardStatsDTO.RecentOrderDTO> recent = serviceOrderRepository.findTop10ByOrderByCreatedAtDesc()
                    .stream()
                    .map(o -> mapToRecentDTO(o, role))
                    .collect(Collectors.toList());
            builder.recentOrders(recent);
        }

        return builder.build();
    }

    private DashboardStatsDTO.RecentOrderDTO mapToRecentDTO(ServiceOrder order, String role) {
        DashboardStatsDTO.RecentOrderDTO.RecentOrderDTOBuilder b = DashboardStatsDTO.RecentOrderDTO.builder()
                .id(order.getId())
                .clientName(order.getClient().getCompanyName())
                .machineName(order.getMachine().getModel())
                .status(order.getStatus())
                .technicianName(order.getTechnician().getNome())
                .technicianPaymentStatus(order.getTechnicianPaymentStatus())
                .openedAt(order.getOpenedAt() != null ? order.getOpenedAt().format(DATE_FORMATTER) : null);

        // Técnico não vê valores financeiros
        if (!"TECNICO".equals(role)) {
            b.totalValue(order.getTotalValue());
        }

        return b.build();
    }
}
