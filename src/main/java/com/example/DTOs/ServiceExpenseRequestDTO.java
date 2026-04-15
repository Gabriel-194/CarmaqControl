package com.example.DTOs;

import com.example.Domain.ExpenseTypeEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceExpenseRequestDTO {
    
    @NotNull(message = "O tipo da despesa é obrigatório")
    private ExpenseTypeEnum expenseType;

    private Double quantity; // obrigatório se DESLOCAMENTO_KM, informa dias em HOSPEDAGEM/ALIMENTACAO e qtd em PEDAGIO
    
    private Double value;        // obrigatório se NOT DESLOCAMENTO_KM
    
    @Size(max = 300, message = "Descrição não pode exceder 300 caracteres")
    private String description;  // obrigatório se OUTRO
}
