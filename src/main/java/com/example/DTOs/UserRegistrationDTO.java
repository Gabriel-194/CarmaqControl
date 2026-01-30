package com.example.DTOs;

import lombok.Data;

@Data
public class UserRegistrationDTO {
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private String role;
}