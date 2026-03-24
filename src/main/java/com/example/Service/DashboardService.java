package com.example.Service;

import com.example.DTOs.DashboardStatsDTO;
import com.example.Models.ServiceOrder;
import com.example.Models.Usuario;
import com.example.Repository.ClientRepository;
import com.example.Repository.ServiceOrderRepository;
import com.example.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final ServiceOrderService serviceOrderService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats(Integer month, Integer year) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        String role = currentUser.getRole();

        DashboardStatsDTO.DashboardStatsDTOBuilder builder = DashboardStatsDTO.builder();


        if ("TECNICO".equals(role)) {
            // Técnico vê apenas suas OS
            
            if (year != null) {
                final int y = year;
                if (month != null) {
                    final int m = month;
                    builder.totalOrders(serviceOrderRepository.countByTechnicianAndMonthAndYear(currentUser.getId(), m, y));
                    builder.completedOrders(serviceOrderRepository.countByTechnicianAndStatusAndMonthAndYear(currentUser.getId(), "CONCLUIDA", m, y));

                    // Pagamentos recebidos dependem da data de fechamento (conclusão)
                    builder.technicianEarnings(serviceOrderRepository.sumTechnicianPaymentByStatusAndMonthAndYear(currentUser.getId(), "RECEBIDO", m, y));
                    // Pagamentos pendentes dependem da data de abertura (planejamento)
                    builder.technicianPendingPayment(serviceOrderRepository.sumTechnicianPaymentByStatusAndMonthAndYearOpened(currentUser.getId(), "A_RECEBER", m, y));
                } else {
                    // "Todos os meses" do ano
                    builder.totalOrders(serviceOrderRepository.countByTechnicianAndYear(currentUser.getId(), y));
                    builder.completedOrders(serviceOrderRepository.countByTechnicianAndStatusAndYear(currentUser.getId(), "CONCLUIDA", y));

                    builder.technicianEarnings(serviceOrderRepository.sumTechnicianPaymentByStatusAndYear(currentUser.getId(), "RECEBIDO", y));
                    builder.technicianPendingPayment(serviceOrderRepository.sumTechnicianPaymentByStatusAndYearOpened(currentUser.getId(), "A_RECEBER", y));
                }
                
                builder.openOrders(serviceOrderRepository.countByTechnicianAndStatus(currentUser.getId(), "ABERTA")); 
            } else {
                builder.totalOrders(serviceOrderRepository.countByTechnician(currentUser.getId()))
                        .openOrders(serviceOrderRepository.countByTechnicianAndStatus(currentUser.getId(), "ABERTA"))
                        .inProgressOrders(serviceOrderRepository.countByTechnicianAndStatus(currentUser.getId(), "EM_ANDAMENTO"))
                        .completedOrders(serviceOrderRepository.countByTechnicianAndStatus(currentUser.getId(), "CONCLUIDA"));

                builder.technicianPendingPayment(serviceOrderRepository.sumTechnicianPaymentByStatus(currentUser.getId(), "A_RECEBER"));
                builder.technicianEarnings(serviceOrderRepository.sumTechnicianPaymentByStatus(currentUser.getId(), "RECEBIDO"));
            }
            
            // Monta lista de OS recentes do técnico (Top 5)
            List<DashboardStatsDTO.RecentOrderDTO> recent = serviceOrderRepository.findWithFiltersTechnician(
                            currentUser.getId(), null, null, null, null, PageRequest.of(0, 5, Sort.by("createdAt").descending()))
                    .getContent()
                    .stream()
                    .map(o -> mapToRecentDTO(o, role))
                    .collect(Collectors.toList());
            builder.recentOrders(recent);
        } else {
            if (year != null) {
                final int y = year;
                if (month != null) {
                    final int m = month;
                    builder.completedOrders(serviceOrderRepository.countByStatusAndMonthAndYear("CONCLUIDA", m, y))
                            .cancelledOrders(serviceOrderRepository.countByStatusAndMonthAndYear("CANCELADA", m, y))
                            .pendingApprovalPayments(serviceOrderRepository.countPendingApprovalPaymentsByMonthAndYear(m, y));

                    builder.totalRevenue(serviceOrderRepository.sumTotalValueByMonthAndYear(m, y));
                    
                    Double revenue = builder.build().getTotalRevenue() != null ? builder.build().getTotalRevenue() : 0.0;
                    Double techPayments = serviceOrderRepository.sumTechnicianPaymentByMonthAndYear(m, y);
                    techPayments = techPayments != null ? techPayments : 0.0;
                    builder.totalExpenses(techPayments);
                    builder.totalProfit(revenue - techPayments);
                } else {
                    // "Todos os meses" do ano
                    builder.completedOrders(serviceOrderRepository.countByStatusAndYear("CONCLUIDA", y))
                            .cancelledOrders(serviceOrderRepository.countByStatusAndYear("CANCELADA", y))
                            .pendingApprovalPayments(serviceOrderRepository.countPendingApprovalPaymentsByYear(y));

                    builder.totalRevenue(serviceOrderRepository.sumTotalValueByYear(y));
                    
                    Double revenue = builder.build().getTotalRevenue() != null ? builder.build().getTotalRevenue() : 0.0;
                    Double techPayments = serviceOrderRepository.sumTechnicianPaymentByYear(y);
                    techPayments = techPayments != null ? techPayments : 0.0;
                    builder.totalExpenses(techPayments);
                    builder.totalProfit(revenue - techPayments);
                }
                
                builder.totalOrders(serviceOrderRepository.count()) 
                        .openOrders(serviceOrderRepository.countByStatus("ABERTA"))
                        .inProgressOrders(serviceOrderRepository.countByStatus("EM_ANDAMENTO"))
                        .requiresInspectionOrders(serviceOrderRepository.countByStatus("REQUER_INSPECAO"));
                
                builder.monthlyRevenue(builder.build().getTotalRevenue());
            } else {
                // PROPRIETARIO e FINANCEIRO veem métricas globais
                builder.totalOrders(serviceOrderRepository.count())
                        .openOrders(serviceOrderRepository.countByStatus("ABERTA"))
                        .inProgressOrders(serviceOrderRepository.countByStatus("EM_ANDAMENTO"))
                        .completedOrders(serviceOrderRepository.countByStatus("CONCLUIDA"))
                        .cancelledOrders(serviceOrderRepository.countByStatus("CANCELADA"))
                        .requiresInspectionOrders(serviceOrderRepository.countByStatus("REQUER_INSPECAO"))
                        .pendingApprovalPayments(serviceOrderRepository.countPendingApprovalPayments());

                builder.totalRevenue(serviceOrderRepository.sumTotalValueCompleted());
                builder.monthlyRevenue(serviceOrderRepository.sumTotalValueCurrentMonth());
                builder.pendingPayments(serviceOrderRepository.sumTotalValuePending());
                
                Double revenue = builder.build().getTotalRevenue() != null ? builder.build().getTotalRevenue() : 0.0;
                Double totalTechPayments = serviceOrderRepository.sumTotalTechnicianPaymentCompleted();
                builder.totalExpenses(totalTechPayments);
                builder.totalProfit(revenue - totalTechPayments); 
            }
            
            if ("PROPRIETARIO".equals(role)) {
                builder.totalTechnicians(usuarioRepository.countByRole("TECNICO"))
                        .totalClients(clientRepository.count());
            }

            List<DashboardStatsDTO.RecentOrderDTO> recent = serviceOrderRepository.findTop10ByOrderByCreatedAtDesc()
                    .stream()
                    .map(o -> mapToRecentDTO(o, role))
                    .collect(Collectors.toList());
            builder.recentOrders(recent);
        }

        return builder.build();
    }

    private DashboardStatsDTO.RecentOrderDTO mapToRecentDTO(ServiceOrder order, String role) {
        if (order == null) return null;

        DashboardStatsDTO.RecentOrderDTO.RecentOrderDTOBuilder b = DashboardStatsDTO.RecentOrderDTO.builder()
                .id(order.getId())
                .clientName(order.getClient() != null ? order.getClient().getCompanyName() : "Cliente N/A")
                .machineName(order.getMachine() != null ? order.getMachine().getModel() : "Máquina N/A")
                .status(order.getStatus())
                .technicianName(order.getTechnician() != null ? order.getTechnician().getNome() : "Técnico N/A")
                .technicianPaymentStatus(order.getTechnicianPaymentStatus())
                .openedAt(order.getOpenedAt() != null ? order.getOpenedAt().format(DATE_FORMATTER) : "N/A");

        // Técnico não vê valores financeiros totais nas atividades recentes (Regra de Segurança)
        if (!"TECNICO".equals(role)) {
            b.totalValue(serviceOrderService.calculateTotal(order));
        }

        return b.build();
    }
}
