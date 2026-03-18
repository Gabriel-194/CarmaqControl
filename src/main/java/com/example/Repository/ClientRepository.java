package com.example.Repository;

import com.example.Models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    List<Client> findAllByActiveTrue();
    
    List<Client> findByCompanyNameContainingIgnoreCaseAndActiveTrue(String companyName);

    @Query("SELECT DISTINCT c FROM Client c JOIN ServiceOrder os ON os.client = c WHERE os.technician.id = :technicianId AND c.active = true")
    List<Client> findAllByTechnicianId(@Param("technicianId") Long technicianId);
}
