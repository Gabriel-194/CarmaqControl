package com.example.Controller;

import com.example.DTOs.ClientRequestDTO;
import com.example.DTOs.ClientResponseDTO;
import com.example.Service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    // Apenas OWNER e FINANCEIRO podem listar, e talvez TECHNICIAN dependendo de contexto (aqui liberamos para autenticados em geral e refinamos no front)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<List<ClientResponseDTO>> getAllClients(
            @RequestParam(name = "includeInactive", required = false, defaultValue = "false") Boolean includeInactive) {
        return ResponseEntity.ok(clientService.getAllClients(includeInactive));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO dto) {
        ClientResponseDTO response = clientService.createClient(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequestDTO dto) {
        return ResponseEntity.ok(clientService.updateClient(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<Void> reactivateClient(@PathVariable Long id) {
        clientService.reactivateClient(id);
        return ResponseEntity.ok().build();
    }
}
