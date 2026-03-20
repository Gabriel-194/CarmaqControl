package com.example.Service;

import com.example.DTOs.ServiceExpenseListDTO;
import com.example.DTOs.ServiceExpenseRequestDTO;
import com.example.DTOs.ServiceExpenseResponseDTO;
import com.example.Domain.ExpenseTypeEnum;
import com.example.Models.ServiceExpense;
import com.example.Models.ServiceOrder;
import com.example.Models.Usuario;
import com.example.Repository.ServiceExpenseRepository;
import com.example.Repository.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceExpenseService {

    private final ServiceExpenseRepository serviceExpenseRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    
    // Lazy to avoid circular dependency if ServiceOrderService injects this
    @Lazy
    private final ServiceOrderService serviceOrderService;

    @Transactional(readOnly = true)
    public ServiceExpenseListDTO getExpensesByServiceOrderId(Long serviceOrderId) {
        ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));

        validateOwnership(order);

        List<ServiceExpense> expenses = serviceExpenseRepository.findByServiceOrderId(serviceOrderId);

        List<ServiceExpenseResponseDTO> dtoList = expenses.stream().map(this::mapToDTO).collect(Collectors.toList());
        Double total = serviceExpenseRepository.sumTotalByServiceOrderId(serviceOrderId);

        return ServiceExpenseListDTO.builder()
                .expenses(dtoList)
                .totalValue(total != null ? total : 0.0)
                .build();
    }

    @Transactional
    public ServiceExpenseResponseDTO addExpense(Long serviceOrderId, ServiceExpenseRequestDTO dto) {
        ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));

        validateOwnership(order);

        if (!"EM_ANDAMENTO".equals(order.getStatus())) {
            throw new RuntimeException("Só é possível adicionar despesas em OS com status EM_ANDAMENTO");
        }

        ServiceExpense expense = ServiceExpense.builder()
                .serviceOrder(order)
                .expenseType(dto.getExpenseType())
                .build();

        if (dto.getExpenseType() == ExpenseTypeEnum.DESLOCAMENTO_KM) {
            if (dto.getQuantityKm() == null) {
                throw new RuntimeException("Quantidade de Km é obrigatória para despesas de deslocamento");
            }
            expense.setQuantityKm(dto.getQuantityKm());
            expense.setValue(dto.getQuantityKm() * 2.20);
            expense.setDescription(dto.getDescription());
        } else {
            if (dto.getValue() == null) {
                throw new RuntimeException("Valor é obrigatório para este tipo de despesa");
            }
            if (dto.getExpenseType() == ExpenseTypeEnum.OUTRO && (dto.getDescription() == null || dto.getDescription().isBlank())) {
                throw new RuntimeException("Descrição é obrigatória para despesas do tipo OUTRO");
            }
            expense.setValue(dto.getValue());
            expense.setDescription(dto.getDescription());
        }

        expense = serviceExpenseRepository.save(expense);
        serviceOrderService.refreshExpensesValue(order);

        return mapToDTO(expense);
    }

    @Transactional
    public void removeExpense(Long serviceOrderId, Long expenseId) {
        ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));

        validateOwnership(order);

        if (!"EM_ANDAMENTO".equals(order.getStatus())) {
            throw new RuntimeException("Só é possível remover despesas em OS com status EM_ANDAMENTO");
        }

        ServiceExpense expense = serviceExpenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));

        if (!expense.getServiceOrder().getId().equals(serviceOrderId)) {
            throw new RuntimeException("Esta despesa não pertence a esta OS");
        }

        serviceExpenseRepository.delete(expense);
        serviceOrderService.refreshExpensesValue(order);
    }

    private void validateOwnership(ServiceOrder order) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();

        if ("TECNICO".equals(currentUser.getRole())) {
            if (!order.getTechnician().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Acesso negado: esta OS não está atribuída a você");
            }
        }
    }

    private ServiceExpenseResponseDTO mapToDTO(ServiceExpense expense) {
        String label;
        switch (expense.getExpenseType()) {
            case DESLOCAMENTO_KM: label = "Deslocamento"; break;
            case PEDAGIO: label = "Pedágio"; break;
            case ALIMENTACAO: label = "Alimentação"; break;
            case HOSPEDAGEM: label = "Hospedagem"; break;
            case OUTRO: label = "Outro"; break;
            default: label = "Desconhecido";
        }

        return ServiceExpenseResponseDTO.builder()
                .id(expense.getId())
                .serviceOrderId(expense.getServiceOrder().getId())
                .expenseType(expense.getExpenseType().name())
                .quantityKm(expense.getQuantityKm())
                .value(expense.getValue())
                .description(expense.getDescription())
                .expenseTypeLabel(label)
                .build();
    }
}
