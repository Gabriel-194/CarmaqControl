package com.example.DTOs;

import com.example.Models.Usuario;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String role;
    private Boolean ativo;
    private LocalDateTime createdAt;

    // Construtor utilitário para converter de Entidade para DTO
    public UserResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.role = usuario.getRole();
        this.ativo = usuario.getAtivo();
        this.createdAt = usuario.getCreatedAt();
    }
}
