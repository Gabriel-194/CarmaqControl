package com.example.Service;

import com.example.DTOs.ClientRequestDTO;
import com.example.DTOs.ClientResponseDTO;
import com.example.Models.Client;
import com.example.Repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client clientActive;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientActive = Client.builder()
                .id(1L)
                .companyName("Empresa Teste")
                .contactName("Nome Contato")
                .email("teste@empresa.com")
                .active(true)
                .build();
    }

    @Test
    void shouldReturnAllActiveClients() {
        when(clientRepository.findAllByActiveTrue()).thenReturn(List.of(clientActive));

        List<ClientResponseDTO> result = clientService.getAllClients(false);

        assertEquals(1, result.size());
        assertEquals("Empresa Teste", result.get(0).getCompanyName());
    }

    @Test
    void shouldCreateClientSuccessfully() {
        ClientRequestDTO request = ClientRequestDTO.builder()
                .companyName("Nova Empresa")
                .contactName("Novo Contato")
                .email("novo@teste.com")
                .phone("123")
                .cep("01001-000")
                .address("Rua A")
                .build();
        
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client c = invocation.getArgument(0);
            c.setId(2L);
            return c;
        });

        ClientResponseDTO response = clientService.createClient(request);

        assertNotNull(response.getId());
        assertEquals("Nova Empresa", response.getCompanyName());
        assertEquals(true, response.getActive());
    }

    @Test
    void shouldSoftDeleteClient() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(clientActive));
        
        clientService.deleteClient(1L);

        assertFalse(clientActive.getActive());
        verify(clientRepository, times(1)).save(clientActive);
    }
}
