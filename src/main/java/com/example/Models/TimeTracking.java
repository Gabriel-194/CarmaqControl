package com.example.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Entidade JPA para rastreamento de tempos da OS
// Tipos: SAIDA_SEDE, CHEGADA_CLIENTE, TRABALHO, RETORNO_SEDE
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "time_trackings", indexes = {
    @Index(name = "idx_time_so", columnList = "service_order_id")
})
public class TimeTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referência à ordem de serviço
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrder serviceOrder;

    // Tipo do registro de tempo (SAIDA_SEDE, TRABALHO, RETORNO_SEDE)
    @Column(nullable = false, length = 30)
    private String type;

    // Data especifica do trabalho (evita confusões de DateTime)
    @Column(name = "registered_date", nullable = false)
    private java.time.LocalDate registeredDate;

    // Hora de início
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    // Hora de fim (pode ser nulo se ainda está em andamento)
    @Column(name = "end_time")
    private LocalDateTime endTime;

    // Descrição opcional da atividade
    @Column(length = 500)
    private String description;
}
