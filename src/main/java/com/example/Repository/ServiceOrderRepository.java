package com.example.Repository;

import com.example.Models.ServiceOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositório para operações de banco das Ordens de Serviço
@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {

    // Buscar OS por técnico (para TECNICO ver apenas suas OS)
    List<ServiceOrder> findByTechnicianId(Long technicianId);

    // Buscar OS por status
    List<ServiceOrder> findByStatus(String status);

    // Buscar OS por cliente
    List<ServiceOrder> findByClientId(Long clientId);

    // Contagens por status (para dashboard)
    long countByStatus(String status);

    // Contagem total de OS
    long count();

    // Soma de valores totais (para receita)
    @Query("SELECT COALESCE(SUM(so.totalValue), 0) FROM ServiceOrder so WHERE so.status = 'CONCLUIDA'")
    Double sumTotalValueCompleted();

    // Soma de valores do mês corrente
    @Query("SELECT COALESCE(SUM(so.totalValue), 0) FROM ServiceOrder so WHERE so.status = 'CONCLUIDA' AND EXTRACT(MONTH FROM so.closedAt) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM so.closedAt) = EXTRACT(YEAR FROM CURRENT_DATE)")
    Double sumTotalValueCurrentMonth();

    // Soma de valores pendentes (OS abertas ou em andamento)
    @Query("SELECT COALESCE(SUM(so.totalValue), 0) FROM ServiceOrder so WHERE so.status NOT IN ('CONCLUIDA', 'CANCELADA')")
    Double sumTotalValuePending();

    // Buscar OS recentes (limitadas)
    List<ServiceOrder> findTop10ByOrderByCreatedAtDesc();

    // Buscar OS por técnico e status
    List<ServiceOrder> findByTechnicianIdAndStatus(Long technicianId, String status);

    // Soma de pagamentos do técnico por status financeiro
    @Query("SELECT COALESCE(SUM(so.technicianPayment), 0) FROM ServiceOrder so WHERE so.technician.id = :techId AND so.technicianPaymentStatus = :paymentStatus")
    Double sumTechnicianPaymentByStatus(@Param("techId") Long techId, @Param("paymentStatus") String paymentStatus);

    // Soma total de pagamentos do técnico (todas as OS atribuídas)
    @Query("SELECT COALESCE(SUM(so.technicianPayment), 0) FROM ServiceOrder so WHERE so.technician.id = :techId")
    Double sumTechnicianPaymentTotal(@Param("techId") Long techId);
}
