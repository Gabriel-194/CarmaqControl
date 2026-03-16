package com.example.Repository;

import com.example.Models.TimeTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositório para registros de tempo
@Repository
public interface TimeTrackingRepository extends JpaRepository<TimeTracking, Long> {

    // Buscar todos os registros de tempo de uma OS
    List<TimeTracking> findByServiceOrderId(Long serviceOrderId);

    // Buscar por OS e tipo de registro
    List<TimeTracking> findByServiceOrderIdAndType(Long serviceOrderId, String type);
}
