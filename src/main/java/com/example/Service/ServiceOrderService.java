package com.example.Service;

import com.example.DTOs.ServiceOrderRequestDTO;
import com.example.DTOs.ServiceOrderResponseDTO;


import com.example.Models.*;
import com.example.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

// Serviço principal para Ordens de Serviço — contém regras de negócio e filtragem por role
@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ClientRepository clientRepository;
    private final MachineRepository machineRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicePartRepository servicePartRepository;
    private final ServiceExpenseRepository serviceExpenseRepository;
    private final TravelCalculationService travelCalculationService;

    @Transactional(readOnly = true)
    public List<ServiceOrder> getOrdersForExcel(String search, String status, Integer month, Integer year) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        if ("TECNICO".equals(currentUser.getRole())) {
            throw new RuntimeException("Acesso negado: Técnicos não podem exportar relatórios financeiros.");
        }

        return serviceOrderRepository.findWithFiltersUnpaginated(search, status, month, year);
    }

    // Retorna OS filtrando por role do usuário logado e aplicando filtros de busca/data
    @Transactional(readOnly = true)
    public Page<ServiceOrderResponseDTO> getAllServiceOrders(String search, String status, Integer month, Integer year, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        String role = currentUser.getRole();

        Page<ServiceOrder> ordersPage;

        // Filtra por técnico se necessário, senão busca todas com filtros
        if ("TECNICO".equals(role)) {
            ordersPage = serviceOrderRepository.findWithFiltersTechnician(currentUser.getId(), search, status, month, year, pageable);
        } else {
            ordersPage = serviceOrderRepository.findWithFilters(search, status, month, year, pageable);
        }

        return ordersPage.map(order -> mapToDTO(order, role));
    }

    @Transactional(readOnly = true)
    public ServiceOrderResponseDTO getServiceOrderById(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        String role = currentUser.getRole();

        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));

        // TECNICO só pode ver OS atribuída a ele
        if ("TECNICO".equals(role) && !order.getTechnician().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: esta OS não está atribuída a você");
        }

        return mapToDTO(order, role);
    }

    // Busca entidade bruta (usado internamente e no relatório)
    @Transactional(readOnly = true)
    public ServiceOrder findById(Long id) {
        return serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));
    }

    @Transactional
    public ServiceOrderResponseDTO createServiceOrder(ServiceOrderRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        String role = currentUser.getRole();

        Long technicianId;
        if ("TECNICO".equals(role)) {
            // Técnico só pode criar para si mesmo
            technicianId = currentUser.getId();
        } else {
            // Admin pode escolher qualquer técnico
            technicianId = dto.getTechnicianId();
            if (technicianId == null) {
                throw new RuntimeException("ID do técnico é obrigatório");
            }
        }

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com id " + dto.getClientId()));

        Machine machine = machineRepository.findById(dto.getMachineId())
                .orElseThrow(() -> new RuntimeException("Máquina não encontrada com id " + dto.getMachineId()));

        Usuario technician = usuarioRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado com id " + technicianId));

        Double serviceValue = dto.getServiceValue() != null ? dto.getServiceValue() : 0.0;
        
        // Regra de Negócio: OS de Instalação trava o valor pelo cadastro da máquina.
        if ("INSTALACAO".equals(dto.getServiceType())) {
            serviceValue = machine.getInstallationPrice() != null ? machine.getInstallationPrice() : 0.0;
        }

        ServiceOrder order = ServiceOrder.builder()
                .client(client)
                .machine(machine)
                .technician(technician)
                .status("ABERTA")
                .serviceDate(dto.getServiceDate())
                .problemDescription(dto.getProblemDescription())
                .serviceDescription(dto.getServiceDescription())
                .observations(dto.getObservations())
                .serviceType(dto.getServiceType())
                .numeroChamado(dto.getNumeroChamado())
                .manutencaoOrigin(dto.getManutencaoOrigin())
                .serviceValue(serviceValue)
                .partsValue(0.0)
                .expensesValue(0.0)
                .discountValue(dto.getDiscountValue() != null ? dto.getDiscountValue() : 0.0)
                .technicianPaymentStatus("A_RECEBER")
                .build();

        order = serviceOrderRepository.save(order);
        return mapToDTO(order, "PROPRIETARIO");
    }

    // Calcula prévia de valores para o frontend sem persistir no banco
    @Transactional(readOnly = true)
    public ServiceOrderResponseDTO calculatePreview(ServiceOrderRequestDTO dto) {
        // Mapeia DTO para entidade temporária (sem ID)
        ServiceOrder tempOrder = ServiceOrder.builder()
                .serviceValue(dto.getServiceValue() != null ? dto.getServiceValue() : 0.0)
                .discountValue(dto.getDiscountValue() != null ? dto.getDiscountValue() : 0.0)
                .expensesValue(0.0)
                .partsValue(0.0) // No preview de criação, peças começam em zero
                .build();

        return mapToDTO(tempOrder, "PROPRIETARIO");
    }

    @Transactional
    public ServiceOrderResponseDTO updateServiceOrder(Long id, ServiceOrderRequestDTO dto) {
        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));

        if (dto.getProblemDescription() != null) {
            order.setProblemDescription(dto.getProblemDescription());
        }
        if (dto.getServiceDescription() != null) {
            order.setServiceDescription(dto.getServiceDescription());
        }
        if (dto.getObservations() != null) {
            order.setObservations(dto.getObservations());
        }
        if (dto.getServiceDate() != null) {
            order.setServiceDate(dto.getServiceDate());
        }
        if (dto.getServiceType() != null) {
            order.setServiceType(dto.getServiceType());
        }
        if (dto.getNumeroChamado() != null) {
            order.setNumeroChamado(dto.getNumeroChamado());
        }
        if (dto.getManutencaoOrigin() != null) {
            order.setManutencaoOrigin(dto.getManutencaoOrigin());
        }
        
        // Bloquear edição direta do ServiceValue se for manutenção
        if ("MANUTENCAO".equals(order.getServiceType())) {
            // ServiceValue é governado pelas horas registradas.
        } else if (dto.getServiceValue() != null) {
            order.setServiceValue(dto.getServiceValue());
        }
        
        if (dto.getDiscountValue() != null) {
            order.setDiscountValue(dto.getDiscountValue());
        }

        // Atualiza o valor das peças (cacheado na entidade)
        refreshPartsValue(order);

        order = serviceOrderRepository.save(order);
        return mapToDTO(order, "PROPRIETARIO");
    }

    // Aprova o pagamento e transfere para o técnico
    @Transactional
    public ServiceOrderResponseDTO approvePayment(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + orderId));

        if ("TECNICO".equals(currentUser.getRole())) {
            throw new RuntimeException("Acesso negado: apenas o Financeiro ou Proprietário podem aprovar o pagamento.");
        }

        if ("RECEBIDO".equals(order.getTechnicianPaymentStatus())) {
            throw new RuntimeException("Este pagamento já foi aprovado e repassado");
        }

        order.setTechnicianPaymentStatus("RECEBIDO");
        order.setRejectionReason(null);
        order = serviceOrderRepository.save(order);
        return mapToDTO(order, currentUser.getRole());
    }

    // Rejeita o repasse ao técnico com motivo
    @Transactional
    public ServiceOrderResponseDTO rejectPayment(Long orderId, String reason) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + orderId));

        if ("TECNICO".equals(currentUser.getRole())) {
            throw new RuntimeException("Acesso negado.");
        }

        order.setTechnicianPaymentStatus("REJEITADO");
        order.setRejectionReason(reason);
        order = serviceOrderRepository.save(order);
        return mapToDTO(order, currentUser.getRole());
    }

    @Transactional
    public ServiceOrderResponseDTO updateServiceDescription(Long id, String description) {
        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));

        order.setServiceDescription(description);
        order = serviceOrderRepository.save(order);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        
        return mapToDTO(order, currentUser.getRole());
    }


    @Transactional
    public ServiceOrderResponseDTO updateStatus(Long id, String newStatus) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));

        // Regra de Negócio: Admin só pode cancelar. Mudanças operacionais são para técnicos.
        if ("PROPRIETARIO".equals(currentUser.getRole())) {
            if (!"CANCELADA".equals(newStatus)) {
                throw new RuntimeException("Gestores podem apenas cancelar Ordens de Serviço. Mudanças operacionais são permitidas apenas para técnicos.");
            }
        }

        order.setStatus(newStatus);

        // Se concluída, registra data de fechamento e joga pagamento para pendente aprovação
        if ("CONCLUIDA".equals(newStatus)) {
            order.setClosedAt(LocalDateTime.now());
            if ("A_RECEBER".equals(order.getTechnicianPaymentStatus())) {
                order.setTechnicianPaymentStatus("PENDENTE_APROVACAO");
            }
        }

        order = serviceOrderRepository.save(order);
        return mapToDTO(order, "PROPRIETARIO");
    }

    // Atualiza o cache de partsValue na entidade
    public void refreshPartsValue(ServiceOrder order) {
        Double partsTotal = servicePartRepository.sumTotalPartsByServiceOrderId(order.getId());
        order.setPartsValue(partsTotal != null ? partsTotal : 0.0);
        serviceOrderRepository.save(order);
    }

    // Atualiza o cache de expensesValue na entidade
    public void refreshExpensesValue(ServiceOrder order) {
        Double total = serviceExpenseRepository.sumTotalByServiceOrderId(order.getId());
        order.setExpensesValue(total != null ? total : 0.0);
        serviceOrderRepository.save(order);
    }
    
    // Calcula as horas da Tabela de Tempo e reformula o valor do Serviço
    public void refreshLaborValue(ServiceOrder order, List<TimeTracking> times) {
        if ("MANUTENCAO".equals(order.getServiceType())) {
            // Valor padrão do serviço é calculado por horas
            double rateTrabalho = 250.0; // CARMARQ
            if ("VALENTIM".equals(order.getManutencaoOrigin())) {
                rateTrabalho = 185.0;
            }
            
            // Taxa de deslocamento
            double rateDeslocamento = 85.0;
            
            long trabalhoMinutes = 0;
            long deslocamentoMinutes = 0;

            for (TimeTracking tt : times) {
                 if (tt.getStartTime() != null && tt.getEndTime() != null) {
                     long mins = java.time.Duration.between(tt.getStartTime(), tt.getEndTime()).toMinutes();
                     if ("TRABALHO".equals(tt.getType())) {
                         trabalhoMinutes += mins;
                     } else {
                         // Demais tempos (Saída/Retorno da Sede) entram como deslocamento
                         deslocamentoMinutes += mins;
                     }
                 }
            }
            
            double hrsTrabalho = trabalhoMinutes / 60.0;
            double hrsDeslocamento = deslocamentoMinutes / 60.0;
            
            double totalLabor = (hrsTrabalho * rateTrabalho) + (hrsDeslocamento * rateDeslocamento);
            order.setServiceValue(Math.round(totalLabor * 100.0) / 100.0);
            serviceOrderRepository.save(order);
        }
    }

    // Cálculos Dinâmicos (Não persistidos)
    public Double calculateTotal(ServiceOrder order) {
        Double discount = (order.getDiscountValue() != null ? order.getDiscountValue() : 0.0);
        return (order.getServiceValue() != null ? order.getServiceValue() : 0.0) +
               (order.getPartsValue() != null ? order.getPartsValue() : 0.0) +
               (order.getExpensesValue() != null ? order.getExpensesValue() : 0.0) - discount;
    }

    public Double calculateTechnicianPayment(ServiceOrder order) {
        Double serviceBase = (order.getServiceValue() != null ? order.getServiceValue() : 0.0);
        Double expenses = (order.getExpensesValue() != null ? order.getExpensesValue() : 0.0);
        
        // Pagamento do técnico: 10% da Mão de Obra + 100% das Despesas (para todos os tipos de serviço)
        return (serviceBase * 0.10) + expenses;
    }



    // Mapeamento para DTO filtrando por role
    private ServiceOrderResponseDTO mapToDTO(ServiceOrder order, String role) {
        ServiceOrderResponseDTO.ServiceOrderResponseDTOBuilder builder = ServiceOrderResponseDTO.builder()
                .id(order.getId())
                .clientId(order.getClient() != null ? order.getClient().getId() : null)
                .clientName(order.getClient() != null ? order.getClient().getCompanyName() : "N/A")
                .clientAddress(order.getClient() != null ? order.getClient().getAddress() : null)
                .machineId(order.getMachine() != null ? order.getMachine().getId() : null)
                .machineName(order.getMachine() != null ? order.getMachine().getModel() : "N/A")
                .machineType(order.getMachine() != null && order.getMachine().getMachineType() != null ? order.getMachine().getMachineType().name() : null)
                .technicianId(order.getTechnician() != null ? order.getTechnician().getId() : null)
                .technicianName(order.getTechnician() != null ? order.getTechnician().getNome() : "Não Atribuído")
                .status(order.getStatus())
                .serviceDate(order.getServiceDate())
                .problemDescription(order.getProblemDescription())
                .serviceDescription(order.getServiceDescription())
                .observations(order.getObservations())
                .serviceType(order.getServiceType())
                .technicianPaymentStatus(order.getTechnicianPaymentStatus())
                .rejectionReason(order.getRejectionReason())
                .manutencaoOrigin(order.getManutencaoOrigin())
                .numeroChamado(order.getNumeroChamado())
                .openedAt(order.getOpenedAt())
                .closedAt(order.getClosedAt());

        // Calcula valores dinamicamente
        Double totalValue = calculateTotal(order);
        Double techPayment = calculateTechnicianPayment(order);

        // Controle de visibilidade baseado na role
        if ("TECNICO".equals(role)) {
            builder.technicianPayment(techPayment);
            // Demais valores financeiros ficam nulos para o técnico
        } else {
            // PROPRIETARIO e FINANCEIRO veem tudo
            builder.serviceValue(order.getServiceValue())
                    .partsValue(order.getPartsValue())
                    .expensesValue(order.getExpensesValue())
                    .discountValue(order.getDiscountValue())
                    .totalValue(totalValue)
                    .technicianPayment(techPayment)
                    .netProfit(totalValue - techPayment);
        }

        // Adiciona dados logísticos se o cliente tiver coordenadas
        if (order.getClient() != null && order.getClient().getLatitude() != null) {
            Double distance = travelCalculationService.calculateDistance(order.getClient().getLatitude(), order.getClient().getLongitude());
            builder.distanceKm(distance != null ? Math.round(distance * 10.0) / 10.0 : null);
            builder.estimatedMinutes(travelCalculationService.estimateMinutes(distance));
            builder.estimatedTravelCost(travelCalculationService.estimateCost(distance));
        }

        return builder.build();
    }

    // =========================================================================
    // NOVOS MÉTODOS ADICIONADOS
    // =========================================================================

    public com.example.DTOs.ServiceOrderSuggestionDTO generateSuggestions(Long machineId) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("Máquina não encontrada com id " + machineId));

        return com.example.DTOs.ServiceOrderSuggestionDTO.builder()
                .suggestedServiceType("MANUTENCAO")
                .estimatedHours(2.0)
                .hourlyRate(250.0)
                .estimatedServiceValue(500.0)
                .estimatedTechnicianPayment(50.0)
                .machineType(machine.getMachineType() != null ? machine.getMachineType().name() : "Desconhecido")
                .machineModel(machine.getModel())
                .machineBrand("-")
                .machineDescription("-")
                .autoObservation("Sugestão automática gerada com sucesso.")
                .build();
    }

    @org.springframework.transaction.annotation.Transactional
    public ServiceOrderResponseDTO markAsReceived(Long id) {
        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));
        order.setTechnicianPaymentStatus("RECEBIDO");
        order = serviceOrderRepository.save(order);
        
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        return mapToDTO(order, currentUser.getRole());
    }
}
