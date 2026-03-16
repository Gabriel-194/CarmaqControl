package com.example.Service;

import com.example.DTOs.TimeTrackingRequestDTO;
import com.example.DTOs.TimeTrackingResponseDTO;
import com.example.Models.ServiceOrder;
import com.example.Models.TimeTracking;
import com.example.Repository.ServiceOrderRepository;
import com.example.Repository.TimeTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

// Serviço para gerenciamento de registros de tempo
@Service
@RequiredArgsConstructor
public class TimeTrackingService {

    private final TimeTrackingRepository timeTrackingRepository;
    private final ServiceOrderRepository serviceOrderRepository;

    @Transactional(readOnly = true)
    public List<TimeTrackingResponseDTO> getTimesByServiceOrderId(Long serviceOrderId) {
        return timeTrackingRepository.findByServiceOrderId(serviceOrderId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TimeTrackingResponseDTO createTimeTracking(Long serviceOrderId, TimeTrackingRequestDTO dto) {
        ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + serviceOrderId));

        TimeTracking timeTracking = TimeTracking.builder()
                .serviceOrder(order)
                .type(dto.getType())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .description(dto.getDescription())
                .build();

        timeTracking = timeTrackingRepository.save(timeTracking);
        return mapToDTO(timeTracking);
    }

    @Transactional
    public TimeTrackingResponseDTO updateTimeTracking(Long id, TimeTrackingRequestDTO dto) {
        TimeTracking timeTracking = timeTrackingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de tempo não encontrado com id " + id));

        if (dto.getEndTime() != null) {
            timeTracking.setEndTime(dto.getEndTime());
        }
        if (dto.getDescription() != null) {
            timeTracking.setDescription(dto.getDescription());
        }

        timeTracking = timeTrackingRepository.save(timeTracking);
        return mapToDTO(timeTracking);
    }

    @Transactional
    public void deleteTimeTracking(Long id) {
        timeTrackingRepository.deleteById(id);
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
                .startTime(tt.getStartTime())
                .endTime(tt.getEndTime())
                .description(tt.getDescription())
                .durationMinutes(durationMinutes)
                .build();
    }
}
