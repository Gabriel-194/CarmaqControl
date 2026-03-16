package com.example.Service;

import com.example.DTOs.ServicePartRequestDTO;
import com.example.DTOs.ServicePartResponseDTO;
import com.example.Models.ServiceOrder;
import com.example.Models.ServicePart;
import com.example.Repository.ServiceOrderRepository;
import com.example.Repository.ServicePartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// Serviço para gerenciamento de peças de serviço
@Service
@RequiredArgsConstructor
public class ServicePartService {

    private final ServicePartRepository servicePartRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderService serviceOrderService;

    @Transactional(readOnly = true)
    public List<ServicePartResponseDTO> getPartsByServiceOrderId(Long serviceOrderId) {
        return servicePartRepository.findByServiceOrderId(serviceOrderId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServicePartResponseDTO addPart(Long serviceOrderId, ServicePartRequestDTO dto) {
        ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + serviceOrderId));

        // Validação de workflow: só permite adicionar peças se EM_ANDAMENTO
        if (!"EM_ANDAMENTO".equals(order.getStatus())) {
            throw new RuntimeException("Peças só podem ser adicionadas quando a OS está EM_ANDAMENTO");
        }

        ServicePart part = ServicePart.builder()
                .serviceOrder(order)
                .partName(dto.getPartName())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .build();

        part = servicePartRepository.save(part);

        // Recalcula o total da OS após adicionar peça
        serviceOrderService.recalculateTotal(order);

        return mapToDTO(part);
    }

    @Transactional
    public void removePart(Long partId) {
        ServicePart part = servicePartRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Peça não encontrada com id " + partId));

        ServiceOrder order = part.getServiceOrder();

        // Validação de workflow: só permite remover peças se EM_ANDAMENTO
        if (!"EM_ANDAMENTO".equals(order.getStatus())) {
            throw new RuntimeException("Peças só podem ser removidas quando a OS está EM_ANDAMENTO");
        }

        servicePartRepository.deleteById(partId);

        // Recalcula o total da OS após remover peça
        serviceOrderService.recalculateTotal(order);
    }

    private ServicePartResponseDTO mapToDTO(ServicePart part) {
        return ServicePartResponseDTO.builder()
                .id(part.getId())
                .serviceOrderId(part.getServiceOrder().getId())
                .partName(part.getPartName())
                .quantity(part.getQuantity())
                .unitPrice(part.getUnitPrice())
                .totalPrice(part.getQuantity() * part.getUnitPrice())
                .build();
    }
}
