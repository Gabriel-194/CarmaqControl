package com.example.Service;

import com.example.DTOs.CardBreakdownDTO;
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
import java.util.ArrayList;
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
                    builder.completedOrders(serviceOrderRepository.countByTechnicianAndStatusAndMonthAndYear(currentUser.getId(), "CONCLUIDA", m, y)
                                         + serviceOrderRepository.countByTechnicianAndStatusAndMonthAndYear(currentUser.getId(), "PAGO", m, y));

                    // Pagamentos recebidos dependem da data de fechamento (conclusão)
                    builder.technicianEarnings(serviceOrderRepository.sumTechnicianPaymentByStatusAndMonthAndYear(currentUser.getId(), "RECEBIDO", m, y));
                    // Pagamentos pendentes dependem da data de abertura (planejamento)
                    builder.technicianPendingPayment(
                            serviceOrderRepository.sumTechnicianPaymentByStatusAndMonthAndYearOpened(currentUser.getId(), "A_RECEBER", m, y) +
                            serviceOrderRepository.sumTechnicianPaymentByStatusAndMonthAndYearOpened(currentUser.getId(), "PENDENTE_APROVACAO", m, y)
                    );
                } else {
                    // "Todos os meses" do ano
                    builder.totalOrders(serviceOrderRepository.countByTechnicianAndYear(currentUser.getId(), y));
                    builder.completedOrders(serviceOrderRepository.countByTechnicianAndStatusAndYear(currentUser.getId(), "CONCLUIDA", y)
                                         + serviceOrderRepository.countByTechnicianAndStatusAndYear(currentUser.getId(), "PAGO", y));

                    builder.technicianEarnings(serviceOrderRepository.sumTechnicianPaymentByStatusAndYear(currentUser.getId(), "RECEBIDO", y));
                    builder.technicianPendingPayment(
                            serviceOrderRepository.sumTechnicianPaymentByStatusAndYearOpened(currentUser.getId(), "A_RECEBER", y) +
                            serviceOrderRepository.sumTechnicianPaymentByStatusAndYearOpened(currentUser.getId(), "PENDENTE_APROVACAO", y)
                    );
                }
                
                builder.openOrders(serviceOrderRepository.countByTechnicianAndStatus(currentUser.getId(), "ABERTA")); 
                builder.inProgressOrders(serviceOrderRepository.countByTechnicianAndStatus(currentUser.getId(), "EM_ANDAMENTO")); 
            } else {
                builder.totalOrders(serviceOrderRepository.countByTechnician(currentUser.getId()))
                        .openOrders(serviceOrderRepository.countByTechnicianAndStatus(currentUser.getId(), "ABERTA"))
                        .inProgressOrders(serviceOrderRepository.countByTechnicianAndStatus(currentUser.getId(), "EM_ANDAMENTO"))
                        .completedOrders(serviceOrderRepository.countByTechnicianAndStatus(currentUser.getId(), "CONCLUIDA")
                                         + serviceOrderRepository.countByTechnicianAndStatus(currentUser.getId(), "PAGO"));

                builder.technicianPendingPayment(
                        serviceOrderRepository.sumTechnicianPaymentByStatus(currentUser.getId(), "A_RECEBER") +
                        serviceOrderRepository.sumTechnicianPaymentByStatus(currentUser.getId(), "PENDENTE_APROVACAO")
                );
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
                    builder.completedOrders(serviceOrderRepository.countByStatusAndMonthAndYear("CONCLUIDA", m, y)
                                            + serviceOrderRepository.countByStatusAndMonthAndYear("PAGO", m, y))
                            .cancelledOrders(serviceOrderRepository.countByStatusAndMonthAndYear("CANCELADA", m, y))
                            .comProblemaOrders(serviceOrderRepository.countByStatusAndMonthAndYear("COM_PROBLEMA", m, y))
                            .pendingApprovalPayments(serviceOrderRepository.countPendingApprovalPaymentsByMonthAndYear(m, y));

                    builder.totalRevenue(serviceOrderRepository.sumTotalValueByMonthAndYear(m, y));
                    
                    Double revenue = builder.build().getTotalRevenue() != null ? builder.build().getTotalRevenue() : 0.0;
                    Double techCommissions = serviceOrderRepository.sumTechnicianPaymentByMonthAndYear(m, y);
                    techCommissions = techCommissions != null ? techCommissions : 0.0;
                    Double reimbursements = serviceOrderRepository.sumTotalReimbursementsByMonthAndYear(m, y);
                    reimbursements = reimbursements != null ? reimbursements : 0.0;
                    
                    builder.technicianCommissions(techCommissions)
                           .technicianReimbursements(reimbursements);
                    
                    Double totalExp = techCommissions + reimbursements;
                    
                    // Lucro Real = Revenue - Comissões - Impostos (12%) - Taxa Boleto (3.50 por OS) - Reembolsos
                    long countCompleted = (serviceOrderRepository.countByStatusAndMonthAndYear("CONCLUIDA", m, y)
                                           + serviceOrderRepository.countByStatusAndMonthAndYear("PAGO", m, y));
                    Double taxes = revenue * 0.12;
                    Double bankFees = countCompleted * 3.50;
                    
                    builder.totalExpenses(totalExp + taxes + bankFees);
                    builder.technicianEarnings(totalExp); // Para compatibilidade se usado no front
                    builder.totalProfit(revenue - totalExp - taxes - bankFees); 
                } else {
                    // "Todos os meses" do ano
                    builder.completedOrders(serviceOrderRepository.countByStatusAndYear("CONCLUIDA", y)
                                            + serviceOrderRepository.countByStatusAndYear("PAGO", y))
                            .cancelledOrders(serviceOrderRepository.countByStatusAndYear("CANCELADA", y))
                            .comProblemaOrders(serviceOrderRepository.countByStatusAndYear("COM_PROBLEMA", y))
                            .pendingApprovalPayments(serviceOrderRepository.countPendingApprovalPaymentsByYear(y));

                    builder.totalRevenue(serviceOrderRepository.sumTotalValueByYear(y));
                    
                    Double revenue = builder.build().getTotalRevenue() != null ? builder.build().getTotalRevenue() : 0.0;
                    Double techCommissions = serviceOrderRepository.sumTechnicianPaymentByYear(y);
                    techCommissions = techCommissions != null ? techCommissions : 0.0;
                    Double reimbursements = serviceOrderRepository.sumDedicatedReimbursementByYear(y);
                    reimbursements = reimbursements != null ? reimbursements : 0.0;
                    
                    builder.technicianCommissions(techCommissions)
                           .technicianReimbursements(reimbursements);
                    
                    Double totalExp = techCommissions + reimbursements;

                    // Lucro Real = Revenue - Comissões - Impostos (12%) - Taxa Boleto (3.50 por OS) - Reembolsos
                    long countCompleted = (serviceOrderRepository.countByStatusAndYear("CONCLUIDA", y)
                                           + serviceOrderRepository.countByStatusAndYear("PAGO", y));
                    Double taxes = revenue * 0.12;
                    Double bankFees = countCompleted * 3.50;

                    builder.totalExpenses(totalExp + taxes + bankFees);
                    builder.technicianEarnings(totalExp); // Para compatibilidade se usado no front
                    builder.totalProfit(revenue - totalExp - taxes - bankFees); 
                }
                
                builder.totalOrders(serviceOrderRepository.count()) 
                        .openOrders(serviceOrderRepository.countByStatus("ABERTA"))
                        .inProgressOrders(serviceOrderRepository.countByStatus("EM_ANDAMENTO")
                                         + serviceOrderRepository.countByStatus("Em Andamento")
                                         + serviceOrderRepository.countByStatus("Em Agendamento"))
                        .comProblemaOrders(serviceOrderRepository.countByStatus("COM_PROBLEMA"))
                        .requiresInspectionOrders(serviceOrderRepository.countByStatus("REQUER_INSPECAO"));
                
                builder.monthlyRevenue(builder.build().getTotalRevenue());
            } else {
                // PROPRIETARIO e FINANCEIRO veem métricas globais
                builder.totalOrders(serviceOrderRepository.count())
                        .openOrders(serviceOrderRepository.countByStatus("ABERTA"))
                        .inProgressOrders(serviceOrderRepository.countByStatus("EM_ANDAMENTO")
                                         + serviceOrderRepository.countByStatus("Em Andamento")
                                         + serviceOrderRepository.countByStatus("Em Agendamento"))
                        .completedOrders(serviceOrderRepository.countByStatus("CONCLUIDA")
                                         + serviceOrderRepository.countByStatus("PAGO"))
                        .cancelledOrders(serviceOrderRepository.countByStatus("CANCELADA"))
                        .comProblemaOrders(serviceOrderRepository.countByStatus("COM_PROBLEMA"))
                        .requiresInspectionOrders(serviceOrderRepository.countByStatus("REQUER_INSPECAO"))
                        .pendingApprovalPayments(serviceOrderRepository.countPendingApprovalPayments());

                builder.totalRevenue(serviceOrderRepository.sumTotalValueCompleted());
                builder.monthlyRevenue(serviceOrderRepository.sumTotalValueCurrentMonth(java.time.LocalDate.now().getMonthValue(), java.time.LocalDate.now().getYear()));
                builder.pendingPayments(serviceOrderRepository.sumTotalValuePending());
                
                Double revenue = builder.build().getTotalRevenue() != null ? builder.build().getTotalRevenue() : 0.0;
                Double totalTechCommissions = serviceOrderRepository.sumTotalTechnicianPaymentCompleted();
                totalTechCommissions = totalTechCommissions != null ? totalTechCommissions : 0.0;
                Double totalReimbursements = serviceOrderRepository.sumDedicatedReimbursementCompleted();
                totalReimbursements = totalReimbursements != null ? totalReimbursements : 0.0;
                
                builder.technicianCommissions(totalTechCommissions)
                       .technicianReimbursements(totalReimbursements);
                
                Double totalExp = totalTechCommissions + totalReimbursements;
                
                long countCompleted = builder.build().getCompletedOrders() != null ? builder.build().getCompletedOrders() : 0;
                Double taxes = revenue * 0.12;
                Double bankFees = countCompleted * 3.50;

                builder.totalExpenses(totalExp + taxes + bankFees);
                builder.technicianEarnings(totalExp); 
                builder.totalProfit(revenue - totalExp - taxes - bankFees); 
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
                .osCode(order.getOsCode())
                .numeroChamado(order.getNumeroChamado())
                .clientName(order.getClient() != null ? order.getClient().getCompanyName() : "Cliente N/A")
                .machineName(order.getMachine() != null ? order.getMachine().getModel() : "Máquina N/A")
                .machineSpecs(buildMachineSpecs(order.getMachine()))
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

    private String buildMachineSpecs(com.example.Models.Machine m) {
        if (m == null) return "";
        StringBuilder specs = new StringBuilder();
        
        if (m.getLaserPower() != null) specs.append(m.getLaserPower()).append("W ");
        if (m.getLaserSize() != null) specs.append(m.getLaserSize()).append(" ");
        if (m.getLaserKind() != null) specs.append(m.getLaserKind()).append(" ");
        if (m.getTonnage() != null) specs.append(m.getTonnage()).append("T ");
        if (m.getMachineSize() != null) specs.append(m.getMachineSize()).append(" ");
        if (m.getCommand() != null) specs.append(m.getCommand()).append(" ");
        if (m.getForce() != null) specs.append(m.getForce()).append("kN ");
        if (m.getDiameter() != null) specs.append("Ø").append(m.getDiameter()).append(" ");
        if (m.getRollerCount() != null) specs.append(m.getRollerCount()).append(" Rolos ");
        
        return specs.toString().trim();
    }

    /**
     * Retorna o detalhamento financeiro por OS para um card específico do dashboard.
     * Respeita as regras de visibilidade por role.
     */
    @Transactional(readOnly = true)
    public CardBreakdownDTO getCardBreakdown(String cardType, Integer month, Integer year) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        String role = currentUser.getRole();

        // Técnico não tem acesso ao breakdown financeiro da empresa
        if ("TECNICO".equals(role)) {
            return buildTechnicianBreakdown(cardType, currentUser, month, year);
        }

        // PROPRIETARIO e FINANCEIRO veem o breakdown completo
        List<ServiceOrder> orders = getCompletedOrders(month, year);
        List<CardBreakdownDTO.OrderLineDTO> lines = new ArrayList<>();
        double total = 0.0;

        for (ServiceOrder order : orders) {
            double value = calculateCardValue(order, cardType);
            if (value == 0.0) continue;

            lines.add(CardBreakdownDTO.OrderLineDTO.builder()
                    .id(order.getId())
                    .osCode(order.getOsCode() != null ? order.getOsCode() : "#" + order.getId())
                    .clientName(order.getClient() != null ? order.getClient().getCompanyName() : "N/A")
                    .technicianName(order.getTechnician() != null ? order.getTechnician().getNome() : "N/A")
                    .value(value)
                    .build());
            total += value;
        }

        return CardBreakdownDTO.builder()
                .cardType(cardType)
                .total(total)
                .orders(lines)
                .build();
    }

    // Busca as OS concluídas/pagas filtrando por mês/ano se necessário
    private List<ServiceOrder> getCompletedOrders(Integer month, Integer year) {
        if (year != null && month != null) {
            return serviceOrderRepository.findWithFiltersUnpaginated(null, null, null, null)
                    .stream()
                    .filter(o -> ("CONCLUIDA".equals(o.getStatus()) || "PAGO".equals(o.getStatus())))
                    .filter(o -> {
                        if (o.getClosedAt() == null) return false;
                        return o.getClosedAt().getMonthValue() == month && o.getClosedAt().getYear() == year;
                    })
                    .collect(Collectors.toList());
        } else if (year != null) {
            return serviceOrderRepository.findWithFiltersUnpaginated(null, null, null, null)
                    .stream()
                    .filter(o -> ("CONCLUIDA".equals(o.getStatus()) || "PAGO".equals(o.getStatus())))
                    .filter(o -> {
                        if (o.getClosedAt() == null) return false;
                        return o.getClosedAt().getYear() == year;
                    })
                    .collect(Collectors.toList());
        } else {
            return serviceOrderRepository.findWithFiltersUnpaginated(null, null, null, null)
                    .stream()
                    .filter(o -> ("CONCLUIDA".equals(o.getStatus()) || "PAGO".equals(o.getStatus())))
                    .collect(Collectors.toList());
        }
    }

    // Calcula o valor da OS para um card específico
    private double calculateCardValue(ServiceOrder order, String cardType) {
        Double totalBilled = serviceOrderService.calculateTotal(order);
        Double techPayment = serviceOrderService.calculateTechnicianPayment(order);
        Double reimbursement = order.getReimbursementValue() != null ? order.getReimbursementValue() : 0.0;
        Double taxBase = totalBilled - reimbursement;
        Double taxes = taxBase * 0.12;
        Double boletoFee = 3.50;

        switch (cardType) {
            case "revenue":
                // Faturamento Total = valor total da OS
                return totalBilled;
            case "expenses":
                // Custos Operacionais = Repasse técnico (comissão) + Reembolso + Impostos + Boleto
                double commission = techPayment - reimbursement; // Comissão pura (sem contar reembolso)
                return commission + reimbursement + taxes + boletoFee;
            case "profit":
                // Lucro Líquido = Faturamento - Custos
                double expensesVal = (techPayment - reimbursement) + reimbursement + taxes + boletoFee;
                return totalBilled - expensesVal;
            default:
                return 0.0;
        }
    }

    // Breakdown para técnicos — mostra apenas os valores que ele tem a receber/recebidos
    private CardBreakdownDTO buildTechnicianBreakdown(String cardType, Usuario tech, Integer month, Integer year) {
        List<ServiceOrder> orders;
        if (year != null && month != null) {
            orders = serviceOrderRepository.findWithFiltersTechnician(
                    tech.getId(), null, null, month, year, PageRequest.of(0, 100, Sort.by("createdAt").descending()))
                    .getContent();
        } else if (year != null) {
            orders = serviceOrderRepository.findWithFiltersTechnician(
                    tech.getId(), null, null, null, year, PageRequest.of(0, 100, Sort.by("createdAt").descending()))
                    .getContent();
        } else {
            orders = serviceOrderRepository.findWithFiltersTechnician(
                    tech.getId(), null, null, null, null, PageRequest.of(0, 100, Sort.by("createdAt").descending()))
                    .getContent();
        }

        String paymentStatus = null;
        if ("earnings".equals(cardType)) {
            paymentStatus = "RECEBIDO";
        } else if (!"pending".equals(cardType)) {
            return CardBreakdownDTO.builder().cardType(cardType).total(0.0).orders(List.of()).build();
        }

        List<CardBreakdownDTO.OrderLineDTO> lines = new ArrayList<>();
        double total = 0.0;

        for (ServiceOrder order : orders) {
            if ("pending".equals(cardType)) {
                if (!"A_RECEBER".equals(order.getTechnicianPaymentStatus()) && !"PENDENTE_APROVACAO".equals(order.getTechnicianPaymentStatus())) continue;
            } else {
                if (!paymentStatus.equals(order.getTechnicianPaymentStatus())) continue;
            }
            if ("CANCELADA".equals(order.getStatus())) continue;

            double value = serviceOrderService.calculateTechnicianPayment(order);
            lines.add(CardBreakdownDTO.OrderLineDTO.builder()
                    .id(order.getId())
                    .osCode(order.getOsCode() != null ? order.getOsCode() : "#" + order.getId())
                    .clientName(order.getClient() != null ? order.getClient().getCompanyName() : "N/A")
                    .technicianName(order.getTechnician() != null ? order.getTechnician().getNome() : "N/A")
                    .value(value)
                    .build());
            total += value;
        }

        return CardBreakdownDTO.builder()
                .cardType(cardType)
                .total(total)
                .orders(lines)
                .build();
    }
}
