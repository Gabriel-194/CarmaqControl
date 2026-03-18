package com.example.Service;

import com.example.DTOs.MachineRequestDTO;
import com.example.DTOs.MachineResponseDTO;
import com.example.Models.Machine;
import com.example.Models.Usuario;
import com.example.Repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// Serviço para operações da Biblioteca de Máquinas
@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;

    @Transactional(readOnly = true)
    public List<MachineResponseDTO> getAllMachines(Boolean includeInactive) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        String role = currentUser.getRole();

        List<Machine> machines;

        if ("TECNICO".equals(role)) {
            // Técnicos veem apenas máquinas de suas OSs
            machines = machineRepository.findAllByTechnicianId(currentUser.getId());
        } else if (Boolean.TRUE.equals(includeInactive)) {
            machines = machineRepository.findAll();
        } else {
            machines = machineRepository.findAllByActiveTrue();
        }

        return machines.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MachineResponseDTO getMachineById(Long id) {
        Machine machine = findActiveById(id);
        return mapToDTO(machine);
    }

    @Transactional
    public MachineResponseDTO createMachine(MachineRequestDTO dto) {
        Machine machine = Machine.builder()
                .machineType(dto.getMachineType())
                .model(dto.getModel())
                .brand(dto.getBrand())
                .description(dto.getDescription())
                .hourlyRate(dto.getHourlyRate())
                .estimatedHours(dto.getEstimatedHours())
                .active(true)
                .build();

        machine = machineRepository.save(machine);
        return mapToDTO(machine);
    }

    @Transactional
    public MachineResponseDTO updateMachine(Long id, MachineRequestDTO dto) {
        Machine machine = findActiveById(id);

        machine.setMachineType(dto.getMachineType());
        machine.setModel(dto.getModel());
        machine.setBrand(dto.getBrand());
        machine.setDescription(dto.getDescription());
        machine.setHourlyRate(dto.getHourlyRate());
        machine.setEstimatedHours(dto.getEstimatedHours());

        machine = machineRepository.save(machine);
        return mapToDTO(machine);
    }

    @Transactional
    public void deleteMachine(Long id) {
        Machine machine = findActiveById(id);
        machine.setActive(false);
        machineRepository.save(machine);
    }

    @Transactional
    public void reactivateMachine(Long id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Máquina não encontrada com id " + id));
        machine.setActive(true);
        machineRepository.save(machine);
    }

    private Machine findActiveById(Long id) {
        return machineRepository.findById(id)
                .filter(Machine::getActive)
                .orElseThrow(() -> new RuntimeException("Máquina não encontrada com id " + id));
    }

    private MachineResponseDTO mapToDTO(Machine machine) {
        return MachineResponseDTO.builder()
                .id(machine.getId())
                .machineType(machine.getMachineType())
                .model(machine.getModel())
                .brand(machine.getBrand())
                .description(machine.getDescription())
                .hourlyRate(machine.getHourlyRate())
                .estimatedHours(machine.getEstimatedHours())
                .active(machine.getActive())
                .build();
    }
}
