package com.example.Repository;

import com.example.Models.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositório para operações de banco das Ordens de Serviço
@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {

    // Buscar OS por técnico (Paginado e com EntityGraph para evitar N+1)
    @EntityGraph(attributePaths = {"client", "machine", "technician"})
    Page<ServiceOrder> findByTechnicianId(Long technicianId, Pageable pageable);

    @EntityGraph(attributePaths = {"client", "machine", "technician"})
    Page<ServiceOrder> findByTechnicianIdAndStatus(Long technicianId, String status, Pageable pageable);

    // Buscar todas (Paginado)
    @EntityGraph(attributePaths = {"client", "machine", "technician"})
    Page<ServiceOrder> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"client", "machine", "technician"})
    Page<ServiceOrder> findByStatus(String status, Pageable pageable);

    @EntityGraph(attributePaths = {"client", "machine", "technician"})
    @Query("SELECT so FROM ServiceOrder so WHERE " +
           "(:status IS NULL OR so.status = :status) AND " +
           "(:year IS NULL OR (so.openedAt IS NOT NULL AND EXTRACT(YEAR FROM so.openedAt) = :year)) AND " +
           "(:month IS NULL OR (so.openedAt IS NOT NULL AND EXTRACT(MONTH FROM so.openedAt) = :month)) AND " +
           "(:search IS NULL OR LOWER(so.client.companyName) LIKE LOWER(CAST(CONCAT('%', :search, '%') AS string)) OR " +
           "LOWER(so.numeroChamado) LIKE LOWER(CAST(CONCAT('%', :search, '%') AS string)) OR " +
           "LOWER(so.technician.nome) LIKE LOWER(CAST(CONCAT('%', :search, '%') AS string)) OR " +
           "CAST(so.id AS string) LIKE CAST(CONCAT('%', :search, '%') AS string))")
    Page<ServiceOrder> findWithFilters(
            @Param("search") String search,
            @Param("status") String status,
            @Param("month") Integer month,
            @Param("year") Integer year,
            Pageable pageable);

    @EntityGraph(attributePaths = {"client", "machine", "technician"})
    @Query("SELECT so FROM ServiceOrder so WHERE " +
           "so.technician.id = :techId AND " +
           "(:status IS NULL OR so.status = :status) AND " +
           "(:year IS NULL OR (so.openedAt IS NOT NULL AND EXTRACT(YEAR FROM so.openedAt) = :year)) AND " +
           "(:month IS NULL OR (so.openedAt IS NOT NULL AND EXTRACT(MONTH FROM so.openedAt) = :month)) AND " +
           "(:search IS NULL OR LOWER(so.client.companyName) LIKE LOWER(CAST(CONCAT('%', :search, '%') AS string)) OR " +
           "LOWER(so.numeroChamado) LIKE LOWER(CAST(CONCAT('%', :search, '%') AS string)) OR " +
           "CAST(so.id AS string) LIKE CAST(CONCAT('%', :search, '%') AS string))")
    Page<ServiceOrder> findWithFiltersTechnician(
            @Param("techId") Long techId,
            @Param("search") String search,
            @Param("status") String status,
            @Param("month") Integer month,
            @Param("year") Integer year,
            Pageable pageable);

    // Buscar OS por cliente
    List<ServiceOrder> findByClientId(Long clientId);

    // Contagens por status (para dashboard)
    long countByStatus(String status);

    // Contagem total de OS
    long count();

    // Soma de valores totais (para receita)
    @Query("SELECT COALESCE(SUM(COALESCE(so.serviceValue, 0.0) + COALESCE(so.expensesValue, 0.0) + COALESCE(so.partsValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.status = 'CONCLUIDA'")
    Double sumTotalValueCompleted();

    // Soma de valores do mês corrente
    @Query("SELECT COALESCE(SUM(COALESCE(so.serviceValue, 0.0) + COALESCE(so.expensesValue, 0.0) + COALESCE(so.partsValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.status = 'CONCLUIDA' AND (so.closedAt IS NOT NULL AND EXTRACT(MONTH FROM so.closedAt) = EXTRACT(MONTH FROM CURRENT_DATE) AND EXTRACT(YEAR FROM so.closedAt) = EXTRACT(YEAR FROM CURRENT_DATE))")
    Double sumTotalValueCurrentMonth();

    // Soma de valores pendentes (OS abertas ou em andamento)
    @Query("SELECT COALESCE(SUM(COALESCE(so.serviceValue, 0.0) + COALESCE(so.expensesValue, 0.0) + COALESCE(so.partsValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.status NOT IN ('CONCLUIDA', 'CANCELADA')")
    Double sumTotalValuePending();

    // Buscar OS recentes
    @EntityGraph(attributePaths = {"client", "machine", "technician"})
    List<ServiceOrder> findTop10ByOrderByCreatedAtDesc();

    // Soma de pagamentos do técnico por status financeiro
    @Query("SELECT COALESCE(SUM((COALESCE(so.serviceValue, 0.0) * 0.1) + COALESCE(so.expensesValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.technician.id = :techId AND so.technicianPaymentStatus = :paymentStatus")
    Double sumTechnicianPaymentByStatus(@Param("techId") Long techId, @Param("paymentStatus") String paymentStatus);

    // Soma total de pagamentos do técnico (todas as OS atribuídas)
    @Query("SELECT COALESCE(SUM((COALESCE(so.serviceValue, 0.0) * 0.1) + COALESCE(so.expensesValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.technician.id = :techId")
    Double sumTechnicianPaymentTotal(@Param("techId") Long techId);

    // Queries para Filtro Mensal
    @Query("SELECT COALESCE(SUM(COALESCE(so.serviceValue, 0.0) + COALESCE(so.expensesValue, 0.0) + COALESCE(so.partsValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.status = 'CONCLUIDA' AND (so.closedAt IS NOT NULL AND EXTRACT(MONTH FROM so.closedAt) = :month AND EXTRACT(YEAR FROM so.closedAt) = :year)")
    Double sumTotalValueByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COALESCE(SUM((COALESCE(so.serviceValue, 0.0) * 0.1) + COALESCE(so.expensesValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.status = 'CONCLUIDA' AND (so.closedAt IS NOT NULL AND EXTRACT(MONTH FROM so.closedAt) = :month AND EXTRACT(YEAR FROM so.closedAt) = :year)")
    Double sumTechnicianPaymentByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(so) FROM ServiceOrder so WHERE so.status = :status AND (so.closedAt IS NOT NULL AND EXTRACT(MONTH FROM so.closedAt) = :month AND EXTRACT(YEAR FROM so.closedAt) = :year)")
    long countByStatusAndMonthAndYear(@Param("status") String status, @Param("month") int month, @Param("year") int year);

    @Query("SELECT COALESCE(SUM((COALESCE(so.serviceValue, 0.0) * 0.1) + COALESCE(so.expensesValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.technician.id = :techId AND so.technicianPaymentStatus = :paymentStatus AND (so.closedAt IS NOT NULL AND EXTRACT(MONTH FROM so.closedAt) = :month AND EXTRACT(YEAR FROM so.closedAt) = :year)")
    Double sumTechnicianPaymentByStatusAndMonthAndYear(@Param("techId") Long techId, @Param("paymentStatus") String paymentStatus, @Param("month") int month, @Param("year") int year);

    @Query("SELECT COALESCE(SUM((COALESCE(so.serviceValue, 0.0) * 0.1) + COALESCE(so.expensesValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.technician.id = :techId AND so.technicianPaymentStatus = :paymentStatus AND (so.openedAt IS NOT NULL AND EXTRACT(MONTH FROM so.openedAt) = :month AND EXTRACT(YEAR FROM so.openedAt) = :year)")
    Double sumTechnicianPaymentByStatusAndMonthAndYearOpened(@Param("techId") Long techId, @Param("paymentStatus") String paymentStatus, @Param("month") int month, @Param("year") int year);

    // Queries para Filtro Anual (Todos os meses)
    @Query("SELECT COALESCE(SUM(COALESCE(so.serviceValue, 0.0) + COALESCE(so.expensesValue, 0.0) + COALESCE(so.partsValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.status = 'CONCLUIDA' AND (so.closedAt IS NOT NULL AND EXTRACT(YEAR FROM so.closedAt) = :year)")
    Double sumTotalValueByYear(@Param("year") int year);

    @Query("SELECT COALESCE(SUM((COALESCE(so.serviceValue, 0.0) * 0.1) + COALESCE(so.expensesValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.status = 'CONCLUIDA'")
    Double sumTotalTechnicianPaymentCompleted();

    @Query("SELECT COALESCE(SUM((COALESCE(so.serviceValue, 0.0) * 0.1) + COALESCE(so.expensesValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.status = 'CONCLUIDA' AND (so.closedAt IS NOT NULL AND EXTRACT(YEAR FROM so.closedAt) = :year)")
    Double sumTechnicianPaymentByYear(@Param("year") int year);

    @Query("SELECT COUNT(so) FROM ServiceOrder so WHERE so.status = :status AND (so.closedAt IS NOT NULL AND EXTRACT(YEAR FROM so.closedAt) = :year)")
    long countByStatusAndYear(@Param("status") String status, @Param("year") int year);

    @Query("SELECT COALESCE(SUM((COALESCE(so.serviceValue, 0.0) * 0.1) + COALESCE(so.expensesValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.technician.id = :techId AND so.technicianPaymentStatus = :paymentStatus AND (so.closedAt IS NOT NULL AND EXTRACT(YEAR FROM so.closedAt) = :year)")
    Double sumTechnicianPaymentByStatusAndYear(@Param("techId") Long techId, @Param("paymentStatus") String paymentStatus, @Param("year") int year);

    @Query("SELECT COALESCE(SUM((COALESCE(so.serviceValue, 0.0) * 0.1) + COALESCE(so.expensesValue, 0.0)), 0.0) FROM ServiceOrder so WHERE so.technician.id = :techId AND so.technicianPaymentStatus = :paymentStatus AND so.openedAt IS NOT NULL AND EXTRACT(YEAR FROM so.openedAt) = :year")
    Double sumTechnicianPaymentByStatusAndYearOpened(@Param("techId") Long techId, @Param("paymentStatus") String paymentStatus, @Param("year") int year);

    // Contagens específicas para Dashboard do Técnico
    @Query("SELECT COUNT(so) FROM ServiceOrder so WHERE so.technician.id = :techId")
    long countByTechnician(@Param("techId") Long techId);

    @Query("SELECT COUNT(so) FROM ServiceOrder so WHERE so.technician.id = :techId AND so.status = :status")
    long countByTechnicianAndStatus(@Param("techId") Long techId, @Param("status") String status);

    @Query("SELECT COUNT(so) FROM ServiceOrder so WHERE so.technician.id = :techId AND so.openedAt IS NOT NULL AND EXTRACT(YEAR FROM so.openedAt) = :year")
    long countByTechnicianAndYear(@Param("techId") Long techId, @Param("year") int year);

    @Query("SELECT COUNT(so) FROM ServiceOrder so WHERE so.technician.id = :techId AND so.status = :status AND so.openedAt IS NOT NULL AND EXTRACT(YEAR FROM so.openedAt) = :year")
    long countByTechnicianAndStatusAndYear(@Param("techId") Long techId, @Param("status") String status, @Param("year") int year);

    @Query("SELECT COUNT(so) FROM ServiceOrder so WHERE so.technician.id = :techId AND so.openedAt IS NOT NULL AND EXTRACT(MONTH FROM so.openedAt) = :month AND EXTRACT(YEAR FROM so.openedAt) = :year")
    long countByTechnicianAndMonthAndYear(@Param("techId") Long techId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(so) FROM ServiceOrder so WHERE so.technician.id = :techId AND so.status = :status AND so.openedAt IS NOT NULL AND EXTRACT(MONTH FROM so.openedAt) = :month AND EXTRACT(YEAR FROM so.openedAt) = :year")
    long countByTechnicianAndStatusAndMonthAndYear(@Param("techId") Long techId, @Param("status") String status, @Param("month") int month, @Param("year") int year);

    List<ServiceOrder> findByStatus(String status);
}
