package com.example.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDTO {
    
    private Long id;
    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private String cep;
    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean active;
}
