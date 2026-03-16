package com.example.Service;

import com.example.DTOs.ServiceOrderRequestDTO;
import com.example.DTOs.ServiceOrderResponseDTO;
import com.example.DTOs.ServiceOrderSuggestionDTO;
import com.example.Models.*;
import com.example.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Serviço principal para Ordens de Serviço — contém regras de negócio e filtragem por role
@Service
@RequiredArgsConstructor
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ClientRepository clientRepository;
    private final MachineRepository machineRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicePartRepository servicePartRepository;

    // Retorna OS filtrando por role do usuário logado
    @Transactional(readOnly = true)
    public List<ServiceOrderResponseDTO> getAllServiceOrders(String statusFilter) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        String role = currentUser.getRole();

        List<ServiceOrder> orders;

        // TECNICO vê apenas as OS atribuídas a ele
        if ("TECNICO".equals(role)) {
            if (statusFilter != null && !statusFilter.isBlank()) {
                orders = serviceOrderRepository.findByTechnicianIdAndStatus(currentUser.getId(), statusFilter);
            } else {
                orders = serviceOrderRepository.findByTechnicianId(currentUser.getId());
            }
        } else {
            // PROPRIETARIO e FINANCEIRO veem todas
            if (statusFilter != null && !statusFilter.isBlank()) {
                orders = serviceOrderRepository.findByStatus(statusFilter);
            } else {
                orders = serviceOrderRepository.findAll();
            }
        }

        return orders.stream()
                .map(order -> mapToDTO(order, role))
                .collect(Collectors.toList());
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

    @Transactional
    public ServiceOrderResponseDTO createServiceOrder(ServiceOrderRequestDTO dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com id " + dto.getClientId()));

        Machine machine = machineRepository.findById(dto.getMachineId())
                .orElseThrow(() -> new RuntimeException("Máquina não encontrada com id " + dto.getMachineId()));

        Usuario technician = usuarioRepository.findById(dto.getTechnicianId())
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado com id " + dto.getTechnicianId()));

        // Valor do serviço definido manualmente pelo proprietário
        Double serviceValue = dto.getServiceValue();
        Double travelCost = dto.getTravelCost() != null ? dto.getTravelCost() : 0.0;

        // Cálculo automático do pagamento do técnico (10% do valor do serviço)
        Double technicianPayment = serviceValue * 0.10;

        ServiceOrder order = ServiceOrder.builder()
                .client(client)
                .machine(machine)
                .technician(technician)
                .status("ABERTA")
                .priority(dto.getPriority() != null ? dto.getPriority() : "NORMAL")
                .problemDescription(dto.getProblemDescription())
                .serviceDescription(dto.getServiceDescription())
                .observations(dto.getObservations())
                .serviceType(dto.getServiceType())
                .serviceValue(serviceValue)
                .partsValue(0.0)
                .travelCost(travelCost)
                .totalValue(serviceValue + travelCost)
                .technicianPayment(technicianPayment)
                .technicianPaymentStatus("A_RECEBER")
                .build();

        order = serviceOrderRepository.save(order);
        return mapToDTO(order, "PROPRIETARIO");
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
        if (dto.getTravelCost() != null) {
            order.setTravelCost(dto.getTravelCost());
        }
        if (dto.getPriority() != null) {
            order.setPriority(dto.getPriority());
        }
        if (dto.getServiceType() != null) {
            order.setServiceType(dto.getServiceType());
        }
        if (dto.getServiceValue() != null) {
            order.setServiceValue(dto.getServiceValue());
            // Recalcula pagamento do técnico (10%)
            order.setTechnicianPayment(dto.getServiceValue() * 0.10);
        }

        // Recalcula o total
        recalculateTotal(order);

        order = serviceOrderRepository.save(order);
        return mapToDTO(order, "PROPRIETARIO");
    }

    // Marca o pagamento do técnico como RECEBIDO (ação irreversível)
    @Transactional
    public ServiceOrderResponseDTO markAsReceived(Long orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        ServiceOrder order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + orderId));

        // Verifica se o usuário atual é o técnico desta OS
        if (!order.getTechnician().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: esta OS não está atribuída a você");
        }

        // Verifica se já foi marcado como RECEBIDO (ação irreversível)
        if ("RECEBIDO".equals(order.getTechnicianPaymentStatus())) {
            throw new RuntimeException("Este pagamento já foi marcado como recebido e não pode ser alterado");
        }

        order.setTechnicianPaymentStatus("RECEBIDO");
        order = serviceOrderRepository.save(order);
        return mapToDTO(order, "TECNICO");
    }

    @Transactional
    public ServiceOrderResponseDTO updateStatus(Long id, String newStatus) {
        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));

        order.setStatus(newStatus);

        // Se concluída, registra data de fechamento
        if ("CONCLUIDA".equals(newStatus)) {
            order.setClosedAt(LocalDateTime.now());
        }

        order = serviceOrderRepository.save(order);
        return mapToDTO(order, "PROPRIETARIO");
    }

    // Recalcula o valor total considerando peças
    public void recalculateTotal(ServiceOrder order) {
        Double partsTotal = servicePartRepository.sumTotalPartsByServiceOrderId(order.getId());
        order.setPartsValue(partsTotal);
        order.setTotalValue(order.getServiceValue() + partsTotal + order.getTravelCost());
        serviceOrderRepository.save(order);
    }

    // Gera sugestões automáticas baseadas na máquina selecionada
    @Transactional(readOnly = true)
    public ServiceOrderSuggestionDTO generateSuggestions(Long machineId) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("Máquina não encontrada com id " + machineId));

        Double estimatedValue = machine.getHourlyRate() * machine.getEstimatedHours();

        // Sugestão de tipo de serviço baseada no tipo da máquina
        String suggestedService = "Manutenção " + machine.getMachineType();

        // Peças comuns sugeridas (pode ser expandido futuramente com dados históricos)
        List<String> suggestedParts = new ArrayList<>();
        String type = machine.getMachineType().toLowerCase();
        if (type.contains("ar condicionado") || type.contains("split") || type.contains("hvac")) {
            suggestedParts.add("Filtro de ar");
            suggestedParts.add("Gás refrigerante R-410A");
            suggestedParts.add("Capacitor");
        } else if (type.contains("compressor")) {
            suggestedParts.add("Válvula de segurança");
            suggestedParts.add("Filtro de óleo");
            suggestedParts.add("Correia");
        } else if (type.contains("refrigerador") || type.contains("geladeira")) {
            suggestedParts.add("Termostato");
            suggestedParts.add("Gás refrigerante R-134a");
            suggestedParts.add("Borracha da porta");
        }

        // Observação automática
        String autoObs = String.format(
                "Serviço estimado para %s %s (%s). Valor hora: R$ %.2f | Estimativa: %.1fh | Total estimado: R$ %.2f",
                machine.getMachineType(), machine.getModel(),
                machine.getBrand() != null ? machine.getBrand() : "Marca não informada",
                machine.getHourlyRate(), machine.getEstimatedHours(), estimatedValue
        );

        return ServiceOrderSuggestionDTO.builder()
                .suggestedServiceType(suggestedService)
                .estimatedServiceValue(estimatedValue)
                .estimatedHours(machine.getEstimatedHours())
                .hourlyRate(machine.getHourlyRate())
                .machineType(machine.getMachineType())
                .machineModel(machine.getModel())
                .machineBrand(machine.getBrand())
                .machineDescription(machine.getDescription())
                .suggestedParts(suggestedParts)
                .autoObservation(autoObs)
                .build();
    }

    // Mapeamento para DTO filtrando por role
    private ServiceOrderResponseDTO mapToDTO(ServiceOrder order, String role) {
        ServiceOrderResponseDTO.ServiceOrderResponseDTOBuilder builder = ServiceOrderResponseDTO.builder()
                .id(order.getId())
                .clientId(order.getClient().getId())
                .clientName(order.getClient().getCompanyName())
                .clientAddress(order.getClient().getAddress())
                .machineId(order.getMachine().getId())
                .machineName(order.getMachine().getModel())
                .machineType(order.getMachine().getMachineType())
                .technicianId(order.getTechnician().getId())
                .technicianName(order.getTechnician().getNome())
                .status(order.getStatus())
                .priority(order.getPriority())
                .problemDescription(order.getProblemDescription())
                .serviceDescription(order.getServiceDescription())
                .observations(order.getObservations())
                .serviceType(order.getServiceType())
                .technicianPaymentStatus(order.getTechnicianPaymentStatus())
                .openedAt(order.getOpenedAt())
                .closedAt(order.getClosedAt());

        // TECNICO não vê valores financeiros totais, apenas o pagamento dele
        if ("TECNICO".equals(role)) {
            builder.technicianPayment(order.getTechnicianPayment());
            // Demais valores ficam null (não expostos)
        } else {
            // PROPRIETARIO e FINANCEIRO veem tudo
            builder.serviceValue(order.getServiceValue())
                    .partsValue(order.getPartsValue())
                    .travelCost(order.getTravelCost())
                    .totalValue(order.getTotalValue())
                    .technicianPayment(order.getTechnicianPayment());
        }

        return builder.build();
    }
}
