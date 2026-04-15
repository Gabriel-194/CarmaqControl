package com.example.Controller;

import com.example.DTOs.ServiceExpenseListDTO;
import com.example.DTOs.ServiceExpenseRequestDTO;
import com.example.DTOs.ServiceExpenseResponseDTO;
import com.example.Service.ServiceExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-orders/{serviceOrderId}/expenses")
@RequiredArgsConstructor
public class ServiceExpenseController {

    private final ServiceExpenseService serviceExpenseService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<ServiceExpenseListDTO> getExpenses(
            @PathVariable(name = "serviceOrderId") Long serviceOrderId) {
        return ResponseEntity.ok(serviceExpenseService.getExpensesByServiceOrderId(serviceOrderId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<ServiceExpenseResponseDTO> addExpense(
            @PathVariable(name = "serviceOrderId") Long serviceOrderId,
            @Valid @RequestBody ServiceExpenseRequestDTO dto) {
        return new ResponseEntity<>(serviceExpenseService.addExpense(serviceOrderId, dto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{expenseId}")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<Void> removeExpense(
            @PathVariable(name = "serviceOrderId") Long serviceOrderId,
            @PathVariable(name = "expenseId") Long expenseId) {
        serviceExpenseService.removeExpense(serviceOrderId, expenseId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{expenseId}")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<ServiceExpenseResponseDTO> updateExpense(
            @PathVariable(name = "serviceOrderId") Long serviceOrderId,
            @PathVariable(name = "expenseId") Long expenseId,
            @Valid @RequestBody ServiceExpenseRequestDTO dto) {
        return ResponseEntity.ok(serviceExpenseService.updateExpense(serviceOrderId, expenseId, dto));
    }
}
