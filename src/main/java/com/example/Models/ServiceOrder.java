package com.example.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Entidade JPA para Ordens de Serviço (OS)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "service_orders", indexes = {
    @Index(name = "idx_so_status", columnList = "status"),
    @Index(name = "idx_so_technician", columnList = "technician_id"),
    @Index(name = "idx_so_client", columnList = "client_id"),
    @Index(name = "idx_so_closed_at", columnList = "closed_at")
})
public class ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com o cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Relacionamento com a máquina
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    // Técnico responsável pelo serviço
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Usuario technician;

    // Status da OS: ABERTA, EM_ANDAMENTO, CONCLUIDA, CANCELADA, REQUER_INSPECAO
    @Column(nullable = false, length = 30)
    private String status;

    // Data programada para o atendimento
    @Column(name = "service_date")
    private LocalDate serviceDate;

    // Descrição do problema relatado
    @Column(name = "problem_description", length = 2000)
    private String problemDescription;

    // Descrição do serviço realizado (preenchido pelo técnico)
    @Column(name = "service_description", length = 2000)
    private String serviceDescription;

    // Observações adicionais
    @Column(length = 1000)
    private String observations;

    // Tipo de serviço definido manualmente pelo proprietário
    @Column(name = "service_type", length = 200)
    private String serviceType;

    // Valor do serviço (mão de obra) — definido manualmente pelo proprietário
    @Builder.Default
    @Column(name = "service_value")
    private Double serviceValue = 0.0;

    // Valor total das peças utilizadas
    @Builder.Default
    @Column(name = "parts_value")
    private Double partsValue = 0.0;

    // Custo de deslocamento até o cliente
    @Builder.Default
    @Column(name = "travel_cost")
    private Double travelCost = 0.0;

    // Valor que será pago/transferido ao técnico (calculado automaticamente: 10% de serviceValue + travelCost)
    // O total faturado (totalValue) também é calculado dinamicamente: serviceValue + travelCost + partsValue
    // Estes campos não são mais persistidos diretamente no banco para garantir dinamismo.

    // Status do pagamento do técnico: A_RECEBER ou RECEBIDO
    @Builder.Default
    @Column(name = "technician_payment_status", length = 20)
    private String technicianPaymentStatus = "A_RECEBER";

    // Data de abertura
    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    // Data de fechamento/conclusão
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (openedAt == null) {
            openedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
