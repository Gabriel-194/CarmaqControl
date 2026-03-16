package com.example.DTOs;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nome;
    private String email;
    private String telefone;
    private String role;
    // A senha é opcional na atualização. Se for vazia ou nula, não atualiza.
    private String senha;
}
