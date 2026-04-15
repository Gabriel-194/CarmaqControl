package com.example.Service;

import com.example.DTOs.UserRegistrationDTO;
import com.example.DTOs.UserResponseDTO;
import com.example.DTOs.UserUpdateDTO;
import com.example.Models.Usuario;
import com.example.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    public List<UserResponseDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    public UserResponseDTO findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));
        return new UserResponseDTO(usuario);
    }

    public UserResponseDTO createUsuario(UserRegistrationDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario novoUsuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .role(dto.getRole().toUpperCase())
                .telefone(dto.getTelefone())
                .ativo(true)
                .build();

        Usuario saved = usuarioRepository.save(novoUsuario);
        return new UserResponseDTO(saved);
    }

    public UserResponseDTO updateUsuario(Long id, UserUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            usuario.setNome(dto.getNome());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            // Verifica se o novo email já existe em outro usuário
            if (!usuario.getEmail().equals(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email já cadastrado em outra conta");
            }
            usuario.setEmail(dto.getEmail());
        }
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            usuario.setRole(dto.getRole().toUpperCase());
        }
        if (dto.getTelefone() != null) {
            usuario.setTelefone(dto.getTelefone());
        }

        // Atualiza a senha somente se foi enviada (não blank)
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
            usuario.setFailedLoginAttempts(0);
            usuario.setAccountLockedUntil(null);
        }

        Usuario updated = usuarioRepository.save(usuario);
        return new UserResponseDTO(updated);
    }

    public void deleteUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    public void restoreUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    // Busca apenas técnicos ativos (para seleção na criação de OS)
    public List<UserResponseDTO> findTechnicians() {
        return usuarioRepository.findByRoleAndAtivoTrue("TECNICO").stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }
}