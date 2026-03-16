package com.example.Controller;

import com.example.DTOs.MachineRequestDTO;
import com.example.DTOs.MachineResponseDTO;
import com.example.Service.MachineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller para CRUD de máquinas — apenas PROPRIETARIO pode criar/editar/excluir
@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<List<MachineResponseDTO>> getAllMachines(
            @RequestParam(required = false, defaultValue = "false") Boolean includeInactive) {
        return ResponseEntity.ok(machineService.getAllMachines(includeInactive));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<MachineResponseDTO> getMachineById(@PathVariable Long id) {
        return ResponseEntity.ok(machineService.getMachineById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<MachineResponseDTO> createMachine(@Valid @RequestBody MachineRequestDTO dto) {
        MachineResponseDTO response = machineService.createMachine(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<MachineResponseDTO> updateMachine(@PathVariable Long id, @Valid @RequestBody MachineRequestDTO dto) {
        return ResponseEntity.ok(machineService.updateMachine(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<Void> deleteMachine(@PathVariable Long id) {
        machineService.deleteMachine(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivate")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<Void> reactivateMachine(@PathVariable Long id) {
        machineService.reactivateMachine(id);
        return ResponseEntity.ok().build();
    }
}
