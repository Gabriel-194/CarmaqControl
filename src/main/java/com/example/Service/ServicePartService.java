package com.example.Service;

import com.example.DTOs.ServicePartListDTO;
import com.example.DTOs.ServicePartRequestDTO;
import com.example.DTOs.ServicePartResponseDTO;
import com.example.Models.ServiceOrder;
import com.example.Models.ServicePart;
import com.example.Models.Usuario;
import com.example.Repository.ServiceOrderRepository;
import com.example.Repository.ServicePartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

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
    public ServicePartListDTO getPartsByServiceOrderId(Long serviceOrderId) {
        validateOsOwnership(serviceOrderId);
        List<ServicePartResponseDTO> parts = servicePartRepository.findByServiceOrderId(serviceOrderId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        Double total = parts.stream().mapToDouble(p -> p.getTotalPrice() != null ? p.getTotalPrice() : 0.0).sum();
        
        return ServicePartListDTO.builder()
                .parts(parts)
                .totalValue(total)
                .build();
    }

    @Transactional
    public ServicePartResponseDTO addPart(Long serviceOrderId, ServicePartRequestDTO dto) {
        validateOsOwnership(serviceOrderId);
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
        serviceOrderService.refreshPartsValue(order);

        return mapToDTO(part);
    }

    @Transactional
    public void removePart(Long partId) {
        ServicePart part = servicePartRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Peça não encontrada com id " + partId));

        ServiceOrder order = part.getServiceOrder();
        validateOsOwnership(order.getId());

        // Validação de workflow: só permite remover peças se EM_ANDAMENTO
        if (!"EM_ANDAMENTO".equals(order.getStatus())) {
            throw new RuntimeException("Peças só podem ser removidas quando a OS está EM_ANDAMENTO");
        }

        servicePartRepository.deleteById(partId);

        // Recalcula o total da OS após remover peça
        serviceOrderService.refreshPartsValue(order);
    }

    private void validateOsOwnership(Long serviceOrderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        
        Usuario user = (Usuario) auth.getPrincipal();
        
        // Se for Técnico, verifica se ele é o dono da OS
        if ("TECNICO".equals(user.getRole())) {
            ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                    .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));
            
            if (order.getTechnician() == null || !order.getTechnician().getId().equals(user.getId())) {
                throw new AccessDeniedException("Você não tem permissão para acessar dados desta Ordem de Serviço");
            }
        }
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
