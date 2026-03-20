package com.example.Service;

import com.example.DTOs.ServiceOrderRequestDTO;
import com.example.DTOs.ServiceOrderResponseDTO;
import com.example.DTOs.ServiceOrderSuggestionDTO;
import com.example.Domain.MachineTypeEnum;
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
import java.util.ArrayList;
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
                .displacementKm(dto.getDisplacementKm() != null ? dto.getDisplacementKm() : 0.0)
                .serviceValue(serviceValue)
                .partsValue(0.0)
                .expensesValue(0.0)
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
        if (dto.getDisplacementKm() != null) {
            order.setDisplacementKm(dto.getDisplacementKm());
        }
        
        // Bloquear edição direta do ServiceValue se for manutenção
        if ("MANUTENCAO".equals(order.getServiceType())) {
            // ServiceValue é governado pelas horas registradas.
        } else if (dto.getServiceValue() != null) {
            order.setServiceValue(dto.getServiceValue());
        }

        // Atualiza o valor das peças (cacheado na entidade)
        refreshPartsValue(order);

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
    public ServiceOrderResponseDTO updateDisplacement(Long id, Double displacementKm) {
        ServiceOrder order = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + id));

        order.setDisplacementKm(displacementKm != null ? displacementKm : 0.0);
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

        // Se concluída, registra data de fechamento
        if ("CONCLUIDA".equals(newStatus)) {
            order.setClosedAt(LocalDateTime.now());
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
            double rate = 250.0; // CARMARQ
            if ("VALENTIM".equals(order.getManutencaoOrigin())) {
                rate = 185.0;
            }
            
            long totalMinutes = 0;
            for (TimeTracking tt : times) {
                 if ("TRABALHO".equals(tt.getType()) && tt.getStartTime() != null && tt.getEndTime() != null) {
                     totalMinutes += java.time.Duration.between(tt.getStartTime(), tt.getEndTime()).toMinutes();
                 }
            }
            
            double hours = totalMinutes / 60.0;
            order.setServiceValue(Math.round((hours * rate) * 100.0) / 100.0);
            serviceOrderRepository.save(order);
        }
    }

    // Cálculos Dinâmicos (Não persistidos)
    public Double calculateTotal(ServiceOrder order) {
        Double displacementValue = (order.getDisplacementKm() != null ? order.getDisplacementKm() * 2.20 : 0.0);
        return (order.getServiceValue() != null ? order.getServiceValue() : 0.0) +
               (order.getPartsValue() != null ? order.getPartsValue() : 0.0) +
               (order.getExpensesValue() != null ? order.getExpensesValue() : 0.0) +
               displacementValue;
    }

    public Double calculateTechnicianPayment(ServiceOrder order) {
        Double serviceBase = (order.getServiceValue() != null ? order.getServiceValue() : 0.0);
        Double expenses = (order.getExpensesValue() != null ? order.getExpensesValue() : 0.0);
        Double displacementValue = (order.getDisplacementKm() != null ? order.getDisplacementKm() * 2.20 : 0.0);
        
        // Se for manutenção, o técnico recebe o valor nominal integral (pois é precificado por hora) + deslocamento
        if ("MANUTENCAO".equals(order.getServiceType())) {
            return serviceBase + displacementValue + expenses;
        }
        
        // Se for instalação, o técnico recebe 10% da Mão de Obra + deslocamento + despesas inteiras
        return (serviceBase * 0.10) + displacementValue + expenses;
    }

    // Gera sugestões simplificadas baseadas na máquina selecionada
    @Transactional(readOnly = true)
    public ServiceOrderSuggestionDTO generateSuggestions(Long machineId) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("Máquina não encontrada com id " + machineId));

        // Sugestão de tipo de serviço baseada no tipo da máquina
        String machineTypeStr = machine.getMachineType() != null ? machine.getMachineType().name() : "Desconhecido";
        String suggestedService = "Manutenção " + machineTypeStr;

        // Peças comuns sugeridas (simplificadas para o novo modelo)
        List<String> suggestedParts = new ArrayList<>();
        if (machine.getMachineType() == MachineTypeEnum.LASER || machine.getMachineType() == MachineTypeEnum.GRAVADORA_LASER) {
            suggestedParts.add("Lente de foco");
            suggestedParts.add("Bico de corte");
            suggestedParts.add("Espelho refletor");
        } else if (machine.getMachineType() == MachineTypeEnum.DOBRADEIRA || machine.getMachineType() == MachineTypeEnum.GUILHOTINA) {
            suggestedParts.add("Óleo hidráulico");
            suggestedParts.add("Filtro de óleo");
        }

        // Observação automática (sem valores financeiros)
        String autoObs = String.format(
                "Serviço para máquina: %s | Tipo: %s | Modelo: %s | S/N: %s",
                machine.getName(),
                machineTypeStr,
                machine.getModel(),
                machine.getSerialNumber()
        );

        return ServiceOrderSuggestionDTO.builder()
                .suggestedServiceType(suggestedService)
                .estimatedServiceValue(0.0) // Não há mais estimativa automática por máquina
                .estimatedHours(0.0)
                .hourlyRate(0.0)
                .machineType(machineTypeStr)
                .machineModel(machine.getModel())
                .machineBrand(null) // Removido do modelo
                .machineDescription(machine.getDescription())
                .suggestedParts(suggestedParts)
                .autoObservation(autoObs)
                .estimatedTechnicianPayment(0.0)
                .build();
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
                .manutencaoOrigin(order.getManutencaoOrigin())
                .numeroChamado(order.getNumeroChamado())
                .displacementKm(order.getDisplacementKm())
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
}
