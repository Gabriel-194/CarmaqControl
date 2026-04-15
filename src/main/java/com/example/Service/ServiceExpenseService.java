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

        // Técnicos não podem adicionar despesas após CONCLUIDA.
        // Proprietário/Financeiro só são bloqueados em PAGO ou CANCELADA.

        if ("PAGO".equals(order.getStatus()) || "CANCELADA".equals(order.getStatus())) {
            throw new RuntimeException("Não é possível adicionar despesas em uma OS com status " + order.getStatus());
        }
        

        ServiceExpense expense = ServiceExpense.builder()
                .serviceOrder(order)
                .expenseType(dto.getExpenseType())
                .build();

        if (dto.getExpenseType() == ExpenseTypeEnum.DESLOCAMENTO_KM) {
            if (dto.getQuantity() == null) {
                throw new RuntimeException("Quantidade de Km é obrigatória para despesas de deslocamento");
            }
            expense.setQuantity(dto.getQuantity());
            Double rate = getDisplacementRate(order);
            expense.setValue(dto.getQuantity() * rate);
            expense.setDescription(dto.getDescription());
        } else {
            expense.setQuantity(dto.getQuantity()); // Salva dias/qtd se informado
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

        if ("PAGO".equals(order.getStatus()) || "CANCELADA".equals(order.getStatus())) {
            throw new RuntimeException("Não é possível remover despesas em uma OS com status " + order.getStatus());
        }


        ServiceExpense expense = serviceExpenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));

        if (!expense.getServiceOrder().getId().equals(serviceOrderId)) {
            throw new RuntimeException("Esta despesa não pertence a esta OS");
        }

        serviceExpenseRepository.delete(expense);
        serviceOrderService.refreshExpensesValue(order);
    }

    @Transactional
    public ServiceExpenseResponseDTO updateExpense(Long serviceOrderId, Long expenseId, ServiceExpenseRequestDTO dto) {
        ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));

        validateOwnership(order);

        if ("PAGO".equals(order.getStatus()) || "CANCELADA".equals(order.getStatus())) {
            throw new RuntimeException("Não é possível editar despesas em uma OS com status " + order.getStatus());
        }


        ServiceExpense expense = serviceExpenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada"));

        if (!expense.getServiceOrder().getId().equals(serviceOrderId)) {
            throw new RuntimeException("Esta despesa não pertence a esta OS");
        }

        expense.setExpenseType(dto.getExpenseType());
        if (dto.getExpenseType() == ExpenseTypeEnum.DESLOCAMENTO_KM) {
            if (dto.getQuantity() == null) {
                throw new RuntimeException("Quantidade de Km é obrigatória para despesas de deslocamento");
            }
            expense.setQuantity(dto.getQuantity());
            Double rate = getDisplacementRate(order);
            expense.setValue(dto.getQuantity() * rate);
        } else {
            expense.setQuantity(dto.getQuantity()); // Atualiza dias/qtd se informado
            if (dto.getValue() == null) {
                throw new RuntimeException("Valor é obrigatório para este tipo de despesa");
            }
            if (dto.getExpenseType() == ExpenseTypeEnum.OUTRO && (dto.getDescription() == null || dto.getDescription().isBlank())) {
                throw new RuntimeException("Descrição é obrigatória para despesas do tipo OUTRO");
            }
            expense.setValue(dto.getValue());
        }
        expense.setDescription(dto.getDescription());

        expense = serviceExpenseRepository.save(expense);
        serviceOrderService.refreshExpensesValue(order);

        return mapToDTO(expense);
    }

    private Double getDisplacementRate(ServiceOrder order) {
        if ("INSTALACAO".equals(order.getServiceType())) {
            return 2.20;
        }
        if ("MANUTENCAO".equals(order.getServiceType()) && "VALENTIM".equals(order.getManutencaoOrigin())) {
            return 2.20;
        }
        // Manutenção comum
        return 2.50;
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
            case DESLOCAMENTO_KM: label = "Deslocamento (ida e volta)"; break;
            case PEDAGIO: label = "Pedágio"; break;
            case ALIMENTACAO: label = "Alimentação"; break;
            case HOSPEDAGEM: label = "Hospedagem"; break;
            case PASSAGEM_AEREA: label = "Passagem Aérea"; break;
            case TAXI: label = "Táxi"; break;
            case COMBUSTIVEL: label = "Combustível"; break;
            case ESTACIONAMENTO: label = "Estacionamento"; break;
            case ALUGUEL_CARRO: label = "Aluguel de Carro"; break;
            case MATERIAL: label = "Material"; break;
            case OUTRO: label = "Outro"; break;
            default: label = "Desconhecido";
        }

        return ServiceExpenseResponseDTO.builder()
                .id(expense.getId())
                .serviceOrderId(expense.getServiceOrder().getId())
                .expenseType(expense.getExpenseType().name())
                .quantity(expense.getQuantity())
                .value(expense.getValue())
                .description(expense.getDescription())
                .expenseTypeLabel(label)
                .build();
    }
}
