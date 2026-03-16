package com.example.Repository;

import com.example.Models.ServicePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositório para peças de serviço
@Repository
public interface ServicePartRepository extends JpaRepository<ServicePart, Long> {

    // Buscar todas as peças de uma OS
    List<ServicePart> findByServiceOrderId(Long serviceOrderId);

    // Calcular o valor total de peças de uma OS
    @Query("SELECT COALESCE(SUM(sp.unitPrice * sp.quantity), 0) FROM ServicePart sp WHERE sp.serviceOrder.id = :serviceOrderId")
    Double sumTotalPartsByServiceOrderId(Long serviceOrderId);
}
