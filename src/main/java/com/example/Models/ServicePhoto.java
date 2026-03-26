package com.example.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Entidade JPA para fotos de máquinas associadas à OS
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "service_photos", indexes = {
    @Index(name = "idx_photo_so", columnList = "service_order_id")
})
public class ServicePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referência à ordem de serviço
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrder serviceOrder;

    // Nome original do arquivo
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    // Caminho onde o arquivo foi salvo no servidor
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    // Data/hora do upload
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
