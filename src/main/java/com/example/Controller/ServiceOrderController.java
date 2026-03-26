package com.example.Controller;

import com.example.DTOs.ServiceOrderRequestDTO;
import com.example.DTOs.ServiceOrderResponseDTO;
import com.example.DTOs.ServiceOrderSuggestionDTO;
import com.example.Models.ServiceOrder;
import com.example.Models.Usuario;
import com.example.Service.ReportService;
import com.example.Service.ServiceOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;
    private final ReportService       reportService;

    // ── Sugestões automáticas ────────────────────────────────────────────────
    @GetMapping("/suggestions")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServiceOrderSuggestionDTO> getSuggestions(
            @RequestParam(name = "machineId") Long machineId) {
        return ResponseEntity.ok(serviceOrderService.generateSuggestions(machineId));
    }

    // ── Listagem paginada ────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<org.springframework.data.domain.Page<ServiceOrderResponseDTO>> getAllServiceOrders(
            @RequestParam(name = "search",  required = false) String search,
            @RequestParam(name = "status",  required = false) String status,
            @RequestParam(name = "month",   required = false) Integer month,
            @RequestParam(name = "year",    required = false) Integer year,
            @RequestParam(name = "page",    defaultValue = "0") int page,
            @RequestParam(name = "size",    defaultValue = "10") int size,
            @RequestParam(name = "sort",    defaultValue = "createdAt,desc") String sort) {

        String[] sp = sort.split(",");
        org.springframework.data.domain.Sort sortOrder = org.springframework.data.domain.Sort.by(
                sp.length > 1 && "asc".equalsIgnoreCase(sp[1])
                        ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC, sp[0]);

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size, sortOrder);

        return ResponseEntity.ok(serviceOrderService.getAllServiceOrders(search, status, month, year, pageable));
    }

    // ── Detalhe ──────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> getServiceOrderById(
            @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(serviceOrderService.getServiceOrderById(id));
    }

    // ── Criação ──────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> createServiceOrder(
            @Valid @RequestBody ServiceOrderRequestDTO dto) {
        return new ResponseEntity<>(serviceOrderService.createServiceOrder(dto), HttpStatus.CREATED);
    }

    // ── Preview financeiro ───────────────────────────────────────────────────
    @PostMapping("/preview")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> previewServiceOrder(
            @RequestBody ServiceOrderRequestDTO dto) {
        return ResponseEntity.ok(serviceOrderService.calculatePreview(dto));
    }

    // ── Atualização ──────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<ServiceOrderResponseDTO> updateServiceOrder(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody ServiceOrderRequestDTO dto) {
        return ResponseEntity.ok(serviceOrderService.updateServiceOrder(id, dto));
    }

    // ── Status ───────────────────────────────────────────────────────────────
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> updateStatus(
            @PathVariable(name = "id") Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(serviceOrderService.updateStatus(id, body.get("status")));
    }

    // ── Descrição do serviço ─────────────────────────────────────────────────
    @PutMapping("/{id}/description")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> updateDescription(
            @PathVariable(name = "id") Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(serviceOrderService.updateServiceDescription(id, body.get("serviceDescription")));
    }


    // ── Marcar pagamento como recebido ───────────────────────────────────────
    @PutMapping("/{id}/mark-received")
    @PreAuthorize("hasAuthority('TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> markAsReceived(
            @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(serviceOrderService.markAsReceived(id));
    }

    // ── Aprovar pagamento (Financeiro/Proprietário) ──────────────────────────
    @PutMapping("/{id}/approve-payment")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO')")
    public ResponseEntity<ServiceOrderResponseDTO> approvePayment(
            @PathVariable(name = "id") Long id,
            @RequestBody(required = false) Map<String, Double> payload) {
        
        Double discount = (payload != null && payload.containsKey("discountValue")) 
                          ? payload.get("discountValue") : null;
                          
        return ResponseEntity.ok(serviceOrderService.approvePayment(id, discount));
    }

    // ── Rejeitar pagamento (Financeiro/Proprietário) ──────────────────────────
    @PutMapping("/{id}/reject-payment")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO')")
    public ResponseEntity<ServiceOrderResponseDTO> rejectPayment(
            @PathVariable(name = "id") Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(serviceOrderService.rejectPayment(id, body.get("reason")));
    }


    @GetMapping("/{id}/report")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<byte[]> downloadReport(@PathVariable(name = "id") Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        ServiceOrder order = serviceOrderService.findById(id);

        // Técnico só vê sua própria OS
        if ("TECNICO".equals(currentUser.getRole())
                && !order.getTechnician().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        boolean isInstalacao = "INSTALACAO".equalsIgnoreCase(order.getServiceType());

        if (isInstalacao) {
            byte[] xlsx = reportService.generateInstallationXlsx(order, currentUser.getRole());
            String filename = "OS_" + id + "_Instalacao.xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(xlsx);
        } else {
            byte[] pdf = reportService.generateMaintenancePdf(order, currentUser.getRole());
            String filename = "OS_" + id + "_Manutencao.pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }
    }

    /**
     * GET /api/service-orders/{id}/report/expenses
     *
     * Gera o Relatório de Despesas (despesas.xlsx) separado,
     * disponível para qualquer OS independente do tipo.
     */
    @GetMapping("/{id}/report/expenses")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<byte[]> downloadExpensesReport(@PathVariable(name = "id") Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        ServiceOrder order = serviceOrderService.findById(id);

        if ("TECNICO".equals(currentUser.getRole())
                && !order.getTechnician().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        byte[] xlsx = reportService.generateExpensesXlsx(order);
        String filename = "OS_" + id + "_Despesas.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }
}