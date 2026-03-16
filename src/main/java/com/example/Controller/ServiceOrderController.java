package com.example.Controller;

import com.example.DTOs.ServiceOrderRequestDTO;
import com.example.DTOs.ServiceOrderResponseDTO;
import com.example.DTOs.ServiceOrderSuggestionDTO;
import com.example.Service.ServiceOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Controller principal para Ordens de Serviço
@RestController
@RequestMapping("/api/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    // Endpoint de sugestões automáticas — retorna dados calculados quando uma máquina é selecionada
    @GetMapping("/suggestions")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServiceOrderSuggestionDTO> getSuggestions(@RequestParam Long machineId) {
        return ResponseEntity.ok(serviceOrderService.generateSuggestions(machineId));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<List<ServiceOrderResponseDTO>> getAllServiceOrders(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(serviceOrderService.getAllServiceOrders(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> getServiceOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceOrderService.getServiceOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<ServiceOrderResponseDTO> createServiceOrder(@Valid @RequestBody ServiceOrderRequestDTO dto) {
        ServiceOrderResponseDTO response = serviceOrderService.createServiceOrder(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<ServiceOrderResponseDTO> updateServiceOrder(
            @PathVariable Long id, @Valid @RequestBody ServiceOrderRequestDTO dto) {
        return ResponseEntity.ok(serviceOrderService.updateServiceOrder(id, dto));
    }

    // Endpoint para alterar o status da OS (ex: ABERTA -> EM_ANDAMENTO -> CONCLUIDA)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> updateStatus(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        return ResponseEntity.ok(serviceOrderService.updateStatus(id, newStatus));
    }

    // Endpoint para técnico marcar pagamento como recebido (ação irreversível)
    @PutMapping("/{id}/mark-received")
    @PreAuthorize("hasAuthority('TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> markAsReceived(@PathVariable Long id) {
        return ResponseEntity.ok(serviceOrderService.markAsReceived(id));
    }
}
