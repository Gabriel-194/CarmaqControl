package com.example.Models;

import com.example.Domain.MachineTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Entidade JPA para a Biblioteca de Máquinas refatorada para tipos específicos
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

    // Nome identificador da máquina
    @Column(nullable = false, length = 150)
    private String name;

    // Tipo da máquina (discriminador central)
    @Enumerated(EnumType.STRING)
    @Column(name = "machine_type", nullable = false)
    private MachineTypeEnum machineType;

    // Modelo da máquina
    @Column(nullable = false, length = 150)
    private String model;

    // Número de série (obrigatório para identificação técnica)
    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber;

    // Preço de instalação sugerido para esta máquina
    @Column(name = "installation_price")
    private Double installationPrice;

    // Descrição técnica adicional
    @Column(length = 500)
    private String description;

    // --- CAMPOS ESPECÍFICOS (Nullable na DB, validados por tipo no frontend) ---

    // LASER / GRAVADORA_LASER
    @Column(name = "laser_size")
    private String laserSize;

    @Column(name = "laser_kind") // FECHADA / ABERTA
    private String laserKind;

    @Column(name = "laser_power") // Watts
    private Double laserPower;

    // DOBRADEIRA / GUILHOTINA / CURVADORA_TUBO / METALEIRA / CALANDRA
    @Column(name = "machine_size")
    private String machineSize;

    @Column(name = "tonnage")
    private Double tonnage;

    @Column(name = "command")
    private String command;

    @Column(name = "force")
    private Double force;

    @Column(name = "diameter")
    private Double diameter;

    @Column(name = "roller_count")
    private Integer rollerCount;

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
