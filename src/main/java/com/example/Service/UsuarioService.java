package com.example.Service;

import com.example.DTOs.UserRegistrationDTO;
import com.example.Models.Usuario;
import com.example.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    // Criamos o encoder manualmente aqui para evitar dependência circular
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Obrigatório para o Spring Security validar o login
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    // CRUD Básico
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario createUsuario(UserRegistrationDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario novoUsuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha())) // Hash da senha
                .role(dto.getRole().toUpperCase())
                .telefone(dto.getTelefone())
                .ativo(true)
                .build();

        return usuarioRepository.save(novoUsuario);
    }

    public Usuario updateUsuario(Long id, UserRegistrationDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setRole(dto.getRole().toUpperCase());
        usuario.setTelefone(dto.getTelefone());

        // Atualiza a senha somente se foi enviada
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }

    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}