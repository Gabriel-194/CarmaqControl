package com.example.Models;

import com.example.Domain.ExpenseTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service_expenses", indexes = {
    @Index(name = "idx_expense_so", columnList = "service_order_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceExpense {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrder serviceOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type", nullable = false)
    private ExpenseTypeEnum expenseType;

    // Usado apenas para DESLOCAMENTO_KM
    @Column(name = "quantity_km")
    private Double quantityKm;

    // Valor final da despesa (calculado para km, manual para outros)
    @Column(nullable = false)
    private Double value;

    // Obrigatório apenas para OUTRO
    @Column(length = 300)
    private String description;
}
