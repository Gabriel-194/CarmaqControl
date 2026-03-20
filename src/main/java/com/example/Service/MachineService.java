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

        if (Boolean.TRUE.equals(includeInactive) && !"TECNICO".equals(role)) {
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
                .name(dto.getName())
                .machineType(dto.getMachineType())
                .model(dto.getModel())
                .serialNumber(dto.getSerialNumber())
                .installationPrice(dto.getInstallationPrice())
                .description(dto.getDescription())
                .laserSize(dto.getLaserSize())
                .laserKind(dto.getLaserKind())
                .laserPower(dto.getLaserPower())
                .machineSize(dto.getMachineSize())
                .tonnage(dto.getTonnage())
                .command(dto.getCommand())
                .force(dto.getForce())
                .diameter(dto.getDiameter())
                .rollerCount(dto.getRollerCount())
                .active(true)
                .build();

        machine = machineRepository.save(machine);
        return mapToDTO(machine);
    }

    @Transactional
    public MachineResponseDTO updateMachine(Long id, MachineRequestDTO dto) {
        Machine machine = findActiveById(id);

        machine.setName(dto.getName());
        machine.setMachineType(dto.getMachineType());
        machine.setModel(dto.getModel());
        machine.setSerialNumber(dto.getSerialNumber());
        machine.setInstallationPrice(dto.getInstallationPrice());
        machine.setDescription(dto.getDescription());
        machine.setLaserSize(dto.getLaserSize());
        machine.setLaserKind(dto.getLaserKind());
        machine.setLaserPower(dto.getLaserPower());
        machine.setMachineSize(dto.getMachineSize());
        machine.setTonnage(dto.getTonnage());
        machine.setCommand(dto.getCommand());
        machine.setForce(dto.getForce());
        machine.setDiameter(dto.getDiameter());
        machine.setRollerCount(dto.getRollerCount());

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
                .name(machine.getName())
                .machineType(machine.getMachineType())
                .model(machine.getModel())
                .serialNumber(machine.getSerialNumber())
                .installationPrice(machine.getInstallationPrice())
                .description(machine.getDescription())
                .laserSize(machine.getLaserSize())
                .laserKind(machine.getLaserKind())
                .laserPower(machine.getLaserPower())
                .machineSize(machine.getMachineSize())
                .tonnage(machine.getTonnage())
                .command(machine.getCommand())
                .force(machine.getForce())
                .diameter(machine.getDiameter())
                .rollerCount(machine.getRollerCount())
                .active(machine.getActive())
                .createdAt(machine.getCreatedAt())
                .updatedAt(machine.getUpdatedAt())
                .build();
    }
}
