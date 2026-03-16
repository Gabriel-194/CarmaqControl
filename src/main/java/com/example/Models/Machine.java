package com.example.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Entidade JPA para a Biblioteca de Máquinas
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tipo da máquina (ex: Ar Condicionado, Refrigerador, Compressor)
    @Column(name = "machine_type", nullable = false, length = 100)
    private String machineType;

    // Modelo da máquina
    @Column(nullable = false, length = 150)
    private String model;

    // Marca/fabricante
    @Column(length = 100)
    private String brand;

    // Descrição técnica adicional
    @Column(length = 500)
    private String description;

    // Valor da hora técnica para este tipo de máquina (usado para cálculo automático)
    @Column(name = "hourly_rate", nullable = false)
    private Double hourlyRate;

    // Estimativa de horas de serviço padrão para este tipo de máquina
    @Column(name = "estimated_hours", nullable = false)
    private Double estimatedHours;

    // Soft delete
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
