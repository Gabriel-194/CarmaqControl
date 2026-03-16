package com.example.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entidade JPA para peças utilizadas em uma OS
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "service_parts")
public class ServicePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referência à ordem de serviço
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrder serviceOrder;

    // Nome da peça
    @Column(name = "part_name", nullable = false, length = 200)
    private String partName;

    // Quantidade utilizada
    @Column(nullable = false)
    private Integer quantity;

    // Preço unitário da peça
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;
}
