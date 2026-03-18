package com.example.Repository;

import com.example.Models.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositório para operações de banco da Biblioteca de Máquinas
@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {

    // Busca apenas máquinas ativas
    List<Machine> findAllByActiveTrue();

    // Busca por tipo de máquina
    List<Machine> findByMachineTypeContainingIgnoreCaseAndActiveTrue(String machineType);

    @Query("SELECT DISTINCT m FROM Machine m JOIN ServiceOrder os ON os.machine = m WHERE os.technician.id = :technicianId AND m.active = true")
    List<Machine> findAllByTechnicianId(@Param("technicianId") Long technicianId);
}
