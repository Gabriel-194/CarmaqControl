package com.example.Controller;

import com.example.DTOs.ServiceOrderRequestDTO;
import com.example.DTOs.ServiceOrderResponseDTO;
import com.example.Models.Usuario;
import com.example.Service.ExcelExportService;
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
import java.util.List;

// Controller principal para Ordens de Serviço
@RestController
@RequestMapping("/api/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;
    private final ReportService reportService;
    private final ExcelExportService excelExportService;
    private final com.example.Repository.ServiceExpenseRepository serviceExpenseRepository;



    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<org.springframework.data.domain.Page<ServiceOrderResponseDTO>> getAllServiceOrders(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParts = sort.split(",");
        org.springframework.data.domain.Sort sortOrder = org.springframework.data.domain.Sort.by(
                sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]) ? 
                org.springframework.data.domain.Sort.Direction.ASC : 
                org.springframework.data.domain.Sort.Direction.DESC, 
                sortParts[0]);
                
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortOrder);
        return ResponseEntity.ok(serviceOrderService.getAllServiceOrders(search, status, month, year, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> getServiceOrderById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(serviceOrderService.getServiceOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> createServiceOrder(@Valid @RequestBody ServiceOrderRequestDTO dto) {
        ServiceOrderResponseDTO response = serviceOrderService.createServiceOrder(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Endpoint de preview para frontend "magro" — calcula valores sem salvar
    @PostMapping("/preview")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> previewServiceOrder(@RequestBody ServiceOrderRequestDTO dto) {
        return ResponseEntity.ok(serviceOrderService.calculatePreview(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<ServiceOrderResponseDTO> updateServiceOrder(
            @PathVariable(name = "id") Long id, @Valid @RequestBody ServiceOrderRequestDTO dto) {
        return ResponseEntity.ok(serviceOrderService.updateServiceOrder(id, dto));
    }

    // Endpoint para alterar o status da OS (ex: ABERTA -> EM_ANDAMENTO -> CONCLUIDA)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> updateStatus(
            @PathVariable(name = "id") Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        return ResponseEntity.ok(serviceOrderService.updateStatus(id, newStatus));
    }

    @PutMapping("/{id}/description")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<ServiceOrderResponseDTO> updateDescription(
            @PathVariable(name = "id") Long id, @RequestBody Map<String, String> body) {
        String description = body.get("serviceDescription");
        return ResponseEntity.ok(serviceOrderService.updateServiceDescription(id, description));
    }


    // Endpoint para financeiro aprovar o pagamento repassado ao técnico (ação irreversível)
    @PutMapping("/{id}/approve-payment")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO')")
    public ResponseEntity<ServiceOrderResponseDTO> approvePayment(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(serviceOrderService.approvePayment(id));
    }

    // Endpoint para financeiro rejeitar o repasse
    @PutMapping("/{id}/reject-payment")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO')")
    public ResponseEntity<ServiceOrderResponseDTO> rejectPayment(
            @PathVariable(name = "id") Long id, @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return ResponseEntity.ok(serviceOrderService.rejectPayment(id, reason));
    }

    @GetMapping("/{id}/report")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<byte[]> downloadReport(@PathVariable(name = "id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        
        // Busca a OS real (não o DTO) para gerar o relatório
        com.example.Models.ServiceOrder order = serviceOrderService.findById(id);
        
        // TECNICO só pode baixar relatório de suas próprias OSs
        if ("TECNICO".equals(currentUser.getRole()) && !order.getTechnician().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        byte[] pdf = reportService.generateServiceOrderReport(order, currentUser.getRole());

        String filename = "OS_" + id + "_Relatorio.pdf";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/expense-report")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<byte[]> downloadExpenseReport(@PathVariable(name = "id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        
        com.example.Models.ServiceOrder order = serviceOrderService.findById(id);
        
        if ("TECNICO".equals(currentUser.getRole()) && !order.getTechnician().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        byte[] pdf = reportService.generateExpenseReport(order, currentUser.getRole());

        String filename = "OS_" + id + "_Despesas.pdf";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/export-excel")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO')")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year) throws java.io.IOException {
        
        java.util.List<com.example.Models.ServiceOrder> orders = serviceOrderService.getOrdersForExcel(search, status, month, year);
        byte[] excel = excelExportService.exportServiceOrdersToExcel(orders);

        String filename = "Relatorio_Ordens_de_Servico.xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/{id}/export/instalacao-excel")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<byte[]> exportInstalacaoExcel(@PathVariable(name = "id") Long id) throws java.io.IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        
        com.example.Models.ServiceOrder order = serviceOrderService.findById(id);
        
        if ("TECNICO".equals(currentUser.getRole()) && !order.getTechnician().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        byte[] excel = excelExportService.generateInstalacaoExcel(order);

        String filename = "OS_" + id + "_Entrega_Tecnica.xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/{id}/export/despesas-excel")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<byte[]> exportDespesasExcel(@PathVariable(name = "id") Long id) throws java.io.IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        
        com.example.Models.ServiceOrder order = serviceOrderService.findById(id);
        
        if ("TECNICO".equals(currentUser.getRole()) && !order.getTechnician().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        List<com.example.Models.ServiceExpense> expenses = serviceExpenseRepository.findByServiceOrderId(id);
        byte[] excel = excelExportService.generateDespesasExcel(order, expenses);

        String filename = "OS_" + id + "_Relatorio_Despesas.xlsx";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
