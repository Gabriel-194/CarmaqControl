package com.example.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de requisição para registrar peça usada no serviço
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePartRequestDTO {

    @NotBlank(message = "Nome da peça é obrigatório")
    @Size(max = 200, message = "Nome da peça não pode exceder 200 caracteres")
    private String partName;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    private Integer quantity;

    @NotNull(message = "Preço unitário é obrigatório")
    @Min(value = 0, message = "Preço unitário deve ser positivo")
    private Double unitPrice;
}
