package com.example.Service;

import com.example.DTOs.TimeTrackingListDTO;
import com.example.DTOs.TimeTrackingRequestDTO;
import com.example.DTOs.TimeTrackingResponseDTO;
import com.example.Models.ServiceOrder;
import com.example.Models.TimeTracking;
import com.example.Models.Usuario;
import com.example.Repository.ServiceOrderRepository;
import com.example.Repository.TimeTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Serviço para gerenciamento de registros de tempo
@Service
@RequiredArgsConstructor
public class TimeTrackingService {

    private final TimeTrackingRepository timeTrackingRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderService serviceOrderService;

    @Transactional(readOnly = true)
    public TimeTrackingListDTO getTimesByServiceOrderId(Long serviceOrderId) {
        validateOsOwnership(serviceOrderId);
        List<TimeTrackingResponseDTO> records = timeTrackingRepository.findByServiceOrderId(serviceOrderId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        
        long totalMinutes = records.stream()
                .filter(r -> r.getDurationMinutes() != null)
                .mapToLong(TimeTrackingResponseDTO::getDurationMinutes)
                .sum();
        
        return TimeTrackingListDTO.builder()
                .records(records)
                .totalMinutes(totalMinutes)
                .totalFormatted(formatDuration(totalMinutes))
                .build();
    }

    @Transactional
    public TimeTrackingResponseDTO createTimeTracking(Long serviceOrderId, TimeTrackingRequestDTO dto) {
        validateOsOwnership(serviceOrderId);
        validateMutationPermission();
        ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + serviceOrderId));


        TimeTracking timeTracking = TimeTracking.builder()
                .serviceOrder(order)
                .type(dto.getType())
                .registeredDate(dto.getRegisteredDate())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .description(dto.getDescription())
                .build();

        timeTracking = timeTrackingRepository.save(timeTracking);
        
        // Disparar recálculo do valor da Ordem de Serviço de Manutenção
        List<TimeTracking> times = timeTrackingRepository.findByServiceOrderId(serviceOrderId);
        serviceOrderService.refreshLaborValue(order, times);
        
        return mapToDTO(timeTracking);
    }

    @Transactional
    public TimeTrackingResponseDTO updateTimeTracking(Long id, TimeTrackingRequestDTO dto) {
        TimeTracking timeTracking = timeTrackingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de tempo não encontrado com id " + id));

        validateOsOwnership(timeTracking.getServiceOrder().getId());
        validateMutationPermission();

        if (dto.getStartTime() != null) {
            timeTracking.setStartTime(dto.getStartTime());
        }

        if (dto.getRegisteredDate() != null) {
            timeTracking.setRegisteredDate(dto.getRegisteredDate());
        }
        if (dto.getEndTime() != null) {
            timeTracking.setEndTime(dto.getEndTime());
        }
        if (dto.getDescription() != null) {
            timeTracking.setDescription(dto.getDescription());
        }

        timeTracking = timeTrackingRepository.save(timeTracking);
        
        List<TimeTracking> times = timeTrackingRepository.findByServiceOrderId(timeTracking.getServiceOrder().getId());
        serviceOrderService.refreshLaborValue(timeTracking.getServiceOrder(), times);
        
        return mapToDTO(timeTracking);
    }

    @Transactional
    public void deleteTimeTracking(Long id) {
        TimeTracking timeTracking = timeTrackingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de tempo não encontrado com id " + id));
        
        validateOsOwnership(timeTracking.getServiceOrder().getId());
        validateMutationPermission();
        timeTrackingRepository.deleteById(id);
        
        List<TimeTracking> times = timeTrackingRepository.findByServiceOrderId(timeTracking.getServiceOrder().getId());
        serviceOrderService.refreshLaborValue(timeTracking.getServiceOrder(), times);
    }

    private void validateMutationPermission() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario user = (Usuario) auth.getPrincipal();
        
        if ("PROPRIETARIO".equals(user.getRole())) {
            throw new AccessDeniedException("Gestores não podem lançar registros de tempo. Esta é uma ação operacional do técnico.");
        }
    }

    private void validateOsOwnership(Long serviceOrderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        
        Usuario user = (Usuario) auth.getPrincipal();
        
        if ("TECNICO".equals(user.getRole())) {
            ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                    .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));
            
            if (order.getTechnician() == null || !order.getTechnician().getId().equals(user.getId())) {
                throw new AccessDeniedException("Você não tem permissão para acessar registros de tempo desta Ordem de Serviço");
            }
        }
    }

    private String formatDuration(long minutes) {
        long h = minutes / 60;
        long m = minutes % 60;
        return String.format("%02d:%02d", h, m);
    }

    private TimeTrackingResponseDTO mapToDTO(TimeTracking tt) {
        Long durationMinutes = null;
        if (tt.getStartTime() != null && tt.getEndTime() != null) {
            durationMinutes = Duration.between(tt.getStartTime(), tt.getEndTime()).toMinutes();
        }

        return TimeTrackingResponseDTO.builder()
                .id(tt.getId())
                .serviceOrderId(tt.getServiceOrder().getId())
                .type(tt.getType())
                .registeredDate(tt.getRegisteredDate())
                .startTime(tt.getStartTime())
                .endTime(tt.getEndTime())
                .description(tt.getDescription())
                .durationMinutes(durationMinutes)
                .durationFormatted(durationMinutes != null ? formatDuration(durationMinutes) : "—")
                .startTimeFormatted(formatLocalTime(tt.getStartTime()))
                .endTimeFormatted(formatLocalTime(tt.getEndTime()))
                .build();
    }

    private String formatLocalTime(LocalDateTime dt) {
        if (dt == null) return "—";
        return dt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }
}
