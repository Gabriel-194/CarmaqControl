package com.example.Service;

import com.example.DTOs.ClientRequestDTO;
import com.example.DTOs.ClientResponseDTO;
import com.example.DTOs.TravelEstimateDTO;
import com.example.Models.Client;
import com.example.Models.Usuario;
import com.example.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final TravelCalculationService travelCalculationService;

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getAllClients(Boolean includeInactive) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario currentUser = (Usuario) auth.getPrincipal();
        String role = currentUser.getRole();

        List<Client> clients;

        if (Boolean.TRUE.equals(includeInactive) && !"TECNICO".equals(role)) {
            clients = clientRepository.findAll();
        } else {
            clients = clientRepository.findAllByActiveTrue();
        }

        return clients.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(Long id) {
        Client client = findClientByIdAndActive(id);
        return mapToDTO(client);
    }

    @Transactional
    public ClientResponseDTO createClient(ClientRequestDTO dto) {
        Client client = Client.builder()
                .companyName(dto.getCompanyName())
                .contactName(dto.getContactName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .cep(dto.getCep())
                .address(dto.getAddress())
                .cnpj(dto.getCnpj())
                .ie(dto.getIe())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .active(true)
                .build();

        client = clientRepository.save(client);
        return mapToDTO(client);
    }

    @Transactional
    public ClientResponseDTO updateClient(Long id, ClientRequestDTO dto) {
        Client client = findClientByIdAndActive(id);

        client.setCompanyName(dto.getCompanyName());
        client.setContactName(dto.getContactName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setCep(dto.getCep());
        client.setAddress(dto.getAddress());
        client.setCnpj(dto.getCnpj());
        client.setIe(dto.getIe());
        client.setLatitude(dto.getLatitude());
        client.setLongitude(dto.getLongitude());

        client = clientRepository.save(client);
        return mapToDTO(client);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = findClientByIdAndActive(id);
        client.setActive(false);
        clientRepository.save(client);
    }

    @Transactional
    public void reactivateClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com id " + id));
        client.setActive(true);
        clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public TravelEstimateDTO getTravelEstimate(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (client.getLatitude() == null || client.getLongitude() == null) {
            throw new RuntimeException("Cliente não possui coordenadas cadastradas para cálculo de viagem");
        }

        Double distance = travelCalculationService.calculateDistance(client.getLatitude(), client.getLongitude());
        
        return TravelEstimateDTO.builder()
                .distanceKm(Math.round(distance * 100.0) / 100.0)
                .estimatedMinutes(travelCalculationService.estimateMinutes(distance))
                .estimatedCost(Math.round(travelCalculationService.estimateCost(distance) * 100.0) / 100.0)
                .build();
    }

    private Client findClientByIdAndActive(Long id) {
        return clientRepository.findById(id)
                .filter(Client::getActive)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com id " + id)); // Simplificado sem ResponseStatusException para clareza
    }

    private ClientResponseDTO mapToDTO(Client client) {
        return ClientResponseDTO.builder()
                .id(client.getId())
                .companyName(client.getCompanyName())
                .contactName(client.getContactName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .cep(client.getCep())
                .address(client.getAddress())
                .cnpj(client.getCnpj())
                .ie(client.getIe())
                .latitude(client.getLatitude())
                .longitude(client.getLongitude())
                .active(client.getActive())
                .build();
    }
}
