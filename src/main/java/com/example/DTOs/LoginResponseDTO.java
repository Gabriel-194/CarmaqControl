package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private Boolean success;
    private String message;
    private String nome;
    private String role;
    private Long id;
    private String email;
}