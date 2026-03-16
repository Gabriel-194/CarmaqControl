package com.example.Controller;

import com.example.DTOs.ServicePartRequestDTO;
import com.example.DTOs.ServicePartResponseDTO;
import com.example.Service.ServicePartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller para gerenciamento de peças vinculadas a uma OS
@RestController
@RequestMapping("/api/service-orders/{serviceOrderId}/parts")
@RequiredArgsConstructor
public class ServicePartController {

    private final ServicePartService servicePartService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<List<ServicePartResponseDTO>> getParts(@PathVariable Long serviceOrderId) {
        return ResponseEntity.ok(servicePartService.getPartsByServiceOrderId(serviceOrderId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServicePartResponseDTO> addPart(
            @PathVariable Long serviceOrderId, @Valid @RequestBody ServicePartRequestDTO dto) {
        ServicePartResponseDTO response = servicePartService.addPart(serviceOrderId, dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{partId}")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<Void> removePart(@PathVariable Long serviceOrderId, @PathVariable Long partId) {
        servicePartService.removePart(partId);
        return ResponseEntity.noContent().build();
    }
}
