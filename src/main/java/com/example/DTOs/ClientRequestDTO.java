package com.example.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequestDTO {

    @NotBlank(message = "Nome da empresa é obrigatório")
    @Size(max = 150, message = "Nome da empresa não pode exceder 150 caracteres")
    private String companyName;

    @NotBlank(message = "Nome do contato é obrigatório")
    @Size(max = 150, message = "Nome do contato não pode exceder 150 caracteres")
    private String contactName;

    @Email(message = "E-mail com formato inválido")
    @Size(max = 100, message = "E-mail não pode exceder 100 caracteres")
    private String email;

    @Size(max = 20, message = "Telefone muito longo")
    private String phone;

    @Size(max = 20, message = "CEP muito longo")
    private String cep;

    @Size(max = 255, message = "Endereço excede limite de 255 caracteres")
    private String address;

    @Size(max = 20, message = "CNPJ muito longo")
    private String cnpj;

    @Size(max = 30, message = "IE muito longo")
    private String ie;

    private Double latitude;

    private Double longitude;
}
