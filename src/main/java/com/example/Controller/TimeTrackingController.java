package com.example.Controller;

import com.example.DTOs.TimeTrackingRequestDTO;
import com.example.DTOs.TimeTrackingResponseDTO;
import com.example.Service.TimeTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller para gerenciamento de tempos vinculados a uma OS
@RestController
@RequestMapping("/api/service-orders/{serviceOrderId}/times")
@RequiredArgsConstructor
public class TimeTrackingController {

    private final TimeTrackingService timeTrackingService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<List<TimeTrackingResponseDTO>> getTimes(@PathVariable Long serviceOrderId) {
        return ResponseEntity.ok(timeTrackingService.getTimesByServiceOrderId(serviceOrderId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<TimeTrackingResponseDTO> createTime(
            @PathVariable Long serviceOrderId, @Valid @RequestBody TimeTrackingRequestDTO dto) {
        TimeTrackingResponseDTO response = timeTrackingService.createTimeTracking(serviceOrderId, dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{timeId}")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<TimeTrackingResponseDTO> updateTime(
            @PathVariable Long serviceOrderId, @PathVariable Long timeId, @RequestBody TimeTrackingRequestDTO dto) {
        return ResponseEntity.ok(timeTrackingService.updateTimeTracking(timeId, dto));
    }

    @DeleteMapping("/{timeId}")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<Void> deleteTime(@PathVariable Long serviceOrderId, @PathVariable Long timeId) {
        timeTrackingService.deleteTimeTracking(timeId);
        return ResponseEntity.noContent().build();
    }
}
