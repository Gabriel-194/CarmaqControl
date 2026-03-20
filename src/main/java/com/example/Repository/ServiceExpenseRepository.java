package com.example.Repository;

import com.example.Models.ServiceExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceExpenseRepository extends JpaRepository<ServiceExpense, Long> {
    
    List<ServiceExpense> findByServiceOrderId(Long serviceOrderId);

    @Query("SELECT COALESCE(SUM(e.value), 0) FROM ServiceExpense e WHERE e.serviceOrder.id = :id")
    Double sumTotalByServiceOrderId(@Param("id") Long id);
}
