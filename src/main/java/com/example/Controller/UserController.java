package com.example.Controller;

import com.example.DTOs.UserRegistrationDTO;
import com.example.DTOs.UserResponseDTO;
import com.example.DTOs.UserUpdateDTO;
import com.example.Service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable(name = "id") Long id) {
        try {
            return ResponseEntity.ok(usuarioService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRegistrationDTO dto) {
        try {
            UserResponseDTO created = usuarioService.createUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            // Seria ideal ter um GlobalExceptionHandler retornando ErrorDetails, 
            // mas por hora isso resolve e previne erro 500 desnecessário.
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable(name = "id") Long id, @RequestBody UserUpdateDTO dto) {
        try {
            UserResponseDTO updated = usuarioService.updateUsuario(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "id") Long id) {
        try {
            usuarioService.deleteUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('PROPRIETARIO')")
    public ResponseEntity<Void> restoreUser(@PathVariable(name = "id") Long id) {
        try {
            usuarioService.restoreUsuario(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Endpoint para listar apenas técnicos ativos (usado na criação de OS)
    @GetMapping("/technicians")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<List<UserResponseDTO>> getTechnicians() {
        return ResponseEntity.ok(usuarioService.findTechnicians());
    }
}
