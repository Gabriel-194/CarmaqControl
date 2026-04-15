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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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

        long start = System.currentTimeMillis();
        // Filtra por técnico se necessário, senão busca todas com filtros
        if ("TECNICO".equals(role)) {
            ordersPage = serviceOrderRepository.findWithFiltersTechnician(currentUser.getId(), search, status, month, year, pageable);
        } else {
            ordersPage = serviceOrderRepository.findWithFilters(search, status, month, year, pageable);
        }
        long duration = System.currentTimeMillis() - start;
        if (duration > 500) {
            System.err.println("[PERFORMANCE WARN] Busca de OS demorou " + duration + "ms para o usuário: " + currentUser.getNome());
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
        Double discountValue = dto.getDiscountValue() != null ? dto.getDiscountValue() : 0.0;
        Double travelValue = dto.getTravelValue() != null ? dto.getTravelValue() : 0.0;
        Double displacementValue = dto.getDisplacementValue() != null ? dto.getDisplacementValue() : 0.0;

        // IDOR / Mass Assignment Fix: Técnico não define valores financeiros ou descontos na criação
        if ("TECNICO".equals(role)) {
            serviceValue = 0.0;
            discountValue = 0.0;
            travelValue = 0.0;
            displacementValue = 0.0;
        }

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
                .travelValue(travelValue)
                .displacementValue(displacementValue)
                .reimbursementValue(dto.getReimbursementValue() != null ? dto.getReimbursementValue() : 0.0)
                .discountValue(discountValue)
                .technicianPaymentStatus("A_RECEBER")
                .osCode(generateOsCode(dto.getServiceDate() != null ? dto.getServiceDate() : LocalDate.now()))
                .build();

        order = serviceOrderRepository.save(order);
        
        // IDOR / Info Leak Fix: Retorna o DTO mapeado com a role do usuário atual
        return mapToDTO(order, role);
    }

    // Calcula prévia de valores para o frontend sem persistir no banco
    @Transactional(readOnly = true)
    public ServiceOrderResponseDTO calculatePreview(ServiceOrderRequestDTO dto) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        // Mapeia DTO para entidade temporária (sem ID)
        ServiceOrder tempOrder = ServiceOrder.builder()
                .serviceValue(dto.getServiceValue() != null ? dto.getServiceValue() : 0.0)
                .discountValue(dto.getDiscountValue() != null ? dto.getDiscountValue() : 0.0)
                .travelValue(0.0)
                .displacementValue(0.0)
                .expensesValue(0.0)
                .partsValue(0.0) // No preview de criação, peças começam em zero
                .build();

        return mapToDTO(tempOrder, currentUser.getRole());
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
        
        // Bloquear edição direta do ServiceValue se for manutenção ou se for TÉCNICO
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        String role = currentUser.getRole();

        if ("TECNICO".equals(role)) {
            // Técnicos não podem alterar valores financeiros nem descontos na atualização
            dto.setServiceValue(null);
            dto.setDiscountValue(null);
        }

        if ("MANUTENCAO".equals(order.getServiceType())) {
            // ServiceValue é governado pelas horas registradas.
        } else if (dto.getServiceValue() != null) {
            order.setServiceValue(dto.getServiceValue());
        }
        
        if (dto.getDiscountValue() != null) {
            order.setDiscountValue(dto.getDiscountValue());
        }

        if (dto.getReimbursementValue() != null) {
            order.setReimbursementValue(dto.getReimbursementValue());
        }
        

        // travelValue e displacementValue são atualizados via refreshLaborValue e refreshExpensesValue, não manualmente.

        // Atualiza o valor das peças (cacheado na entidade)
        refreshPartsValue(order);

        order = serviceOrderRepository.save(order);
        return mapToDTO(order, "PROPRIETARIO");
    }

    // Aprova o pagamento e transfere para o técnico
    @Transactional
    public ServiceOrderResponseDTO approvePayment(Long orderId, Double discountValue) {
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

        if (discountValue != null) {
            if (discountValue < 0) {
                throw new RuntimeException("O valor do desconto não pode ser negativo.");
            }
            order.setDiscountValue(discountValue);
        }

        order.setTechnicianPaymentStatus("RECEBIDO");
        order.setStatus("PAGO");
        order.setClosedAt(LocalDateTime.now()); // Garante que a data de fechamento exista se não foi setada na conclusão
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
        order.setStatus("REJEITADA");
        order.setRejectionReason(reason);
        order = serviceOrderRepository.save(order);
        return mapToDTO(order, currentUser.getRole());
    }

    @Transactional
    public ServiceOrderResponseDTO updateServiceDescription(Long id, String description) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        
        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));

        // IDOR Fix: Técnico só edita sua própria OS
        if ("TECNICO".equals(currentUser.getRole()) && !order.getTechnician().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: Você não pode editar uma OS que não está atribuída a você.");
        }

        order.setServiceDescription(description);
        order = serviceOrderRepository.save(order);
        
        return mapToDTO(order, currentUser.getRole());
    }


    @Transactional
    public ServiceOrderResponseDTO updateStatus(Long id, String newStatus) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));

        // IDOR Fix: Técnico só altera status da sua própria OS
        if ("TECNICO".equals(currentUser.getRole()) && !order.getTechnician().getId().equals(currentUser.getId())) {
             throw new RuntimeException("Acesso negado.");
        }

        // Regra de Negócio: Admin/Financeiro podem cancelar, aprovar pagamento ou rejeitar. Mudanças operacionais são para técnicos.
        if ("PROPRIETARIO".equals(currentUser.getRole()) || "FINANCEIRO".equals(currentUser.getRole())) {
            if (!"CANCELADA".equals(newStatus) && !"REJEITADA".equals(newStatus) && !"PAGO".equals(newStatus)) {
                throw new RuntimeException("Gestores e Financeiro podem apenas Cancelar, Rejeitar ou Confirmar Pagamento de Ordens de Serviço.");
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
        
        // Se voltou para revisão (ação do técnico), retoma para pendente aprovação
        if ("EM_REVISAO".equals(newStatus)) {
            order.setTechnicianPaymentStatus("PENDENTE_APROVACAO");
            order.setRejectionReason(null);
        }

        order = serviceOrderRepository.save(order);
        return mapToDTO(order, currentUser.getRole());
    }

    // Atualiza o cache de partsValue na entidade
    public void refreshPartsValue(ServiceOrder order) {
        Double partsTotal = servicePartRepository.sumTotalPartsByServiceOrderId(order.getId());
        order.setPartsValue(partsTotal != null ? partsTotal : 0.0);
        serviceOrderRepository.save(order);
    }

    // Atualiza os caches de expensesValue e displacementValue na entidade
    public void refreshExpensesValue(ServiceOrder order) {
        Double displacementTotal = serviceExpenseRepository.sumDisplacementByServiceOrderId(order.getId());
        Double otherExpensesTotal = serviceExpenseRepository.sumOtherExpensesByServiceOrderId(order.getId());
        
        order.setDisplacementValue(displacementTotal != null ? displacementTotal : 0.0);
        order.setExpensesValue(otherExpensesTotal != null ? otherExpensesTotal : 0.0);
        serviceOrderRepository.save(order);
    }
    
    // Calcula as horas da Tabela de Tempo e reformula o valor do Serviço/Viagem
    public void refreshLaborValue(ServiceOrder order, List<TimeTracking> times) {
        // Valor padrão do serviço é calculado por horas (apenas para MANUTENCAO)
        double rateTrabalho = 250.0; // CARMARQ
        if ("VALENTIM".equals(order.getManutencaoOrigin())) {
            rateTrabalho = 185.0;
        }
        
        // Taxa de deslocamento (85 para Instalação ou Garantia, 185 para o resto)
        double rateDeslocamento = 185.0;
        if ("INSTALACAO".equals(order.getServiceType()) || "VALENTIM".equals(order.getManutencaoOrigin())) {
            rateDeslocamento = 85.0;
        }
        
        long trabalhoMinutes = 0;
        long deslocamentoMinutes = 0;

        for (TimeTracking tt : times) {
             if (tt.getStartTime() != null && tt.getEndTime() != null) {
                 long mins = java.time.Duration.between(tt.getStartTime(), tt.getEndTime()).toMinutes();
                 if ("TRABALHO".equals(tt.getType())) {
                     trabalhoMinutes += mins;
                 } else {
                     deslocamentoMinutes += mins;
                 }
             }
        }
        
        double hrsTrabalho = trabalhoMinutes / 60.0;
        double hrsDeslocamento = deslocamentoMinutes / 60.0;
        
        // travelValue é sempre baseado em horas de deslocamento
        double travelOnly = hrsDeslocamento * rateDeslocamento;
        order.setTravelValue(Math.round(travelOnly * 100.0) / 100.0);
        
        // serviceValue só é recalculado se for MANUTENCAO (Instalação é fixo)
        if ("MANUTENCAO".equals(order.getServiceType())) {
            double laborOnly = hrsTrabalho * rateTrabalho;
            order.setServiceValue(Math.round(laborOnly * 100.0) / 100.0);
        }
        
        serviceOrderRepository.save(order);
    }

    // Cálculos Dinâmicos (Não persistidos)
    public Double calculateTotal(ServiceOrder order) {
        Double discount = (order.getDiscountValue() != null ? order.getDiscountValue() : 0.0);
        return (order.getServiceValue() != null ? order.getServiceValue() : 0.0) +
               (order.getTravelValue() != null ? order.getTravelValue() : 0.0) +
               (order.getDisplacementValue() != null ? order.getDisplacementValue() : 0.0) +
               (order.getPartsValue() != null ? order.getPartsValue() : 0.0) +
               (order.getExpensesValue() != null ? order.getExpensesValue() : 0.0) +
               (order.getReimbursementValue() != null ? order.getReimbursementValue() : 0.0) - discount;
    }

    public Double calculateTechnicianPayment(ServiceOrder order) {
        // Nova Regra: Repasse de 10% sobre o Valor Final Faturado (Bruto - Desconto), descontando os impostos (12%) e o boleto (3.50).
        // Isso serve tanto para Instalação quanto para qualquer Manutenção.
        Double totalBilled = calculateTotal(order);
        Double taxValue = totalBilled * 0.12;
        Double boletoFee = 3.50;
        
        Double netBase = totalBilled - (order.getReimbursementValue() != null ? order.getReimbursementValue() : 0.0) - taxValue - boletoFee;
        if (netBase < 0) {
            netBase = 0.0;
        }
        
        return (netBase * 0.10) + (order.getReimbursementValue() != null ? order.getReimbursementValue() : 0.0);
    }

    public Double calculateGrossValue(ServiceOrder order) {
        return (order.getServiceValue() != null ? order.getServiceValue() : 0.0) +
               (order.getTravelValue() != null ? order.getTravelValue() : 0.0) +
               (order.getDisplacementValue() != null ? order.getDisplacementValue() : 0.0) +
               (order.getPartsValue() != null ? order.getPartsValue() : 0.0) +
               (order.getExpensesValue() != null ? order.getExpensesValue() : 0.0);
    }



    // Mapeamento para DTO filtrando por role
    private ServiceOrderResponseDTO mapToDTO(ServiceOrder order, String role) {
        ServiceOrderResponseDTO.ServiceOrderResponseDTOBuilder builder = ServiceOrderResponseDTO.builder()
                .id(order.getId())
                .clientId(order.getClient() != null ? order.getClient().getId() : null)
                .clientName(order.getClient() != null ? order.getClient().getCompanyName() : "N/A")
                .clientAddress(order.getClient() != null ? order.getClient().getAddress() : null)
                .machineId(order.getMachine() != null ? order.getMachine().getId() : null)
                .machineName(order.getMachine() != null ? order.getMachine().getMachineType() + " " + order.getMachine().getModel() : "N/A")
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
                .closedAt(order.getClosedAt())
                .osCode(ensureOsCode(order));

        // Calcula valores dinamicamente
        Double totalValue = calculateTotal(order);
        Double techPayment = calculateTechnicianPayment(order);
        
        // Deduções Automáticas (Empresa) calculadas sobre o valor faturado desconsiderando o reembolso 100%
        Double taxBase = totalValue - (order.getReimbursementValue() != null ? order.getReimbursementValue() : 0.0);
        Double boletoFee = 3.50;
        Double taxValue = taxBase * 0.12;

        // Controle de visibilidade baseado na role
        if ("TECNICO".equals(role)) {
            builder.technicianPayment(techPayment)
                   .reimbursementValue(order.getReimbursementValue())
                   .serviceValue(order.getServiceValue())
                   .travelValue(order.getTravelValue())
                   .partsValue(order.getPartsValue())
                   .expensesValue(order.getExpensesValue())
                   .displacementValue(order.getDisplacementValue());
        } else {
            // PROPRIETARIO e FINANCEIRO veem tudo
            builder.serviceValue(order.getServiceValue())
                    .travelValue(order.getTravelValue())
                    .displacementValue(order.getDisplacementValue())
                    .partsValue(order.getPartsValue())
                    .expensesValue(order.getExpensesValue())
                    .reimbursementValue(order.getReimbursementValue())
                    .discountValue(order.getDiscountValue())
                    .totalValue(totalValue)
                    .technicianPayment(techPayment)
                    .boletoFee(boletoFee)
                    .taxValue(taxValue)
                    .netProfit(totalValue - techPayment - boletoFee - taxValue);
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
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        // BOLA Check: Técnico só vê sugestões de máquinas que já atendeu
        if ("TECNICO".equals(currentUser.getRole())) {
            boolean hasHistory = serviceOrderRepository.existsByTechnicianIdAndMachineId(currentUser.getId(), machineId);
            if (!hasHistory) {
                throw new org.springframework.security.access.AccessDeniedException("Você não tem permissão para acessar sugestões para esta máquina.");
            }
        }

        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("Máquina não encontrada com id " + machineId));

        double hourlyRate = 250.0; // Padrão
        double estHours = 2.0;

        return com.example.DTOs.ServiceOrderSuggestionDTO.builder()
                .suggestedServiceType("MANUTENCAO")
                .estimatedHours(estHours)
                .hourlyRate(hourlyRate)
                .estimatedServiceValue(hourlyRate * estHours)
                .estimatedTechnicianPayment((hourlyRate * estHours) * 0.1)
                .machineType(machine.getMachineType() != null ? machine.getMachineType().name() : "Desconhecido")
                .machineModel(machine.getModel())
                .autoObservation("Sugestão de manutenção preventiva.")
                .build();
    }

    @org.springframework.transaction.annotation.Transactional
    public ServiceOrderResponseDTO markAsReceived(Long id) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        
        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));
                
        // IDOR Fix
        if ("TECNICO".equals(currentUser.getRole()) && !order.getTechnician().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado.");
        }

        order.setTechnicianPaymentStatus("RECEBIDO");
        order = serviceOrderRepository.save(order);
        
        return mapToDTO(order, currentUser.getRole());
    }

    private synchronized String generateOsCode(LocalDate date) {
        String datePrefix = "OS" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Optional<ServiceOrder> lastOrder = serviceOrderRepository.findTopByOsCodeStartingWithOrderByOsCodeDesc(datePrefix);
        
        if (lastOrder.isEmpty()) {
            return datePrefix + "01";
        }
        
        String lastCode = lastOrder.get().getOsCode();
        try {
            int sequence = Integer.parseInt(lastCode.substring(10)) + 1;
            return datePrefix + String.format("%02d", sequence);
        } catch (Exception e) {
            // Fallback em caso de erro de parsing
            return datePrefix + "01";
        }
    }

    private String ensureOsCode(ServiceOrder order) {
        if (order.getOsCode() != null) {
            return order.getOsCode();
        }
        
        // Se for uma OS antiga sem código, geramos um com base na data de abertura dela
        // Usamos synchronized aqui também para evitar duplicatas durante a migração lazy
        synchronized (this) {
            // Verifica de novo dentro do block synchronized (Double-Checked Locking simplificado)
            ServiceOrder refreshed = serviceOrderRepository.findById(order.getId()).orElse(order);
            if (refreshed.getOsCode() != null) return refreshed.getOsCode();
            
            LocalDateTime dateTime = refreshed.getOpenedAt() != null ? refreshed.getOpenedAt() : (refreshed.getCreatedAt() != null ? refreshed.getCreatedAt() : LocalDateTime.now());
            String code = generateOsCode(dateTime.toLocalDate());
            refreshed.setOsCode(code);
            serviceOrderRepository.save(refreshed);
            order.setOsCode(code); // Atualiza o objeto em memória também
            return code;
        }
    }

    public ServiceOrderResponseDTO updateReimbursement(Long id, Double value) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OS não encontrada."));

        if ("TECNICO".equals(currentUser.getRole()) && !order.getTechnician().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: você só pode alterar reembolsos de suas próprias ordens de serviço.");
        }

        if ("PAGO".equals(order.getStatus()) || "CANCELADA".equals(order.getStatus())) {
            throw new RuntimeException("Não é possível alterar o reembolso de uma OS finalizada ou cancelada.");
        }

        order.setReimbursementValue(value != null ? value : 0.0);
        serviceOrderRepository.save(order);

        return mapToDTO(order, currentUser.getRole());
    }

    // Libera uma OS em inspeção, voltando ao status ABERTA (ação exclusiva do Proprietário)
    @Transactional
    public ServiceOrderResponseDTO releaseInspection(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        if (!"PROPRIETARIO".equals(currentUser.getRole())) {
            throw new RuntimeException("Apenas o Proprietário pode liberar uma OS em inspeção.");
        }

        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));

        if (!"REQUER_INSPECAO".equals(order.getStatus())) {
            throw new RuntimeException("Apenas OS com status 'Requer Inspeção' podem ser liberadas.");
        }

        order.setStatus("ABERTA");
        order = serviceOrderRepository.save(order);
        return mapToDTO(order, currentUser.getRole());
    }
}
