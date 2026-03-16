package com.example.Service;

import com.example.DTOs.LoginRequestDTO;
import com.example.DTOs.LoginResponseDTO;
import com.example.Models.Usuario;
import com.example.Repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    // Usamos o encoder direto para evitar complexidade de injeção circular
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponseDTO login(LoginRequestDTO dto, HttpServletResponse response) {
        // 1. Busca usuário pelo email
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(dto.getEmail());

        if (userOpt.isEmpty()) {
            return new LoginResponseDTO(false, "Email ou senha incorretos", null, null, null, null);
        }

        Usuario user = userOpt.get();

        // 2. Verifica a senha
        if (!passwordEncoder.matches(dto.getSenha(), user.getSenha())) {
            return new LoginResponseDTO(false, "Email ou senha incorretos", null, null, null, null);
        }

        if (!user.getAtivo()) {
            return new LoginResponseDTO(false, "Usuário desativado", null, null, null, null);
        }

        // 3. Gera o Token com claims extras
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole() != null ? user.getRole().toUpperCase() : null);
        extraClaims.put("id", user.getId());
        extraClaims.put("nome", user.getNome());

        // Precisamos adaptar o generateToken para aceitar map ou criar um metodo simples
        // No ArenaConnect ele passa parametros soltos, vamos adaptar aqui:
        String token = jwtService.generateToken(extraClaims, user.getEmail());

        // 4. Cria o Cookie
        Cookie jwtCookie = jwtService.createJwtCookie(token);
        response.addCookie(jwtCookie);

        return new LoginResponseDTO(true, "Login realizado com sucesso", user.getNome(), user.getRole() != null ? user.getRole().toUpperCase() : null, user.getId(), user.getEmail());
    }

    public boolean validateToken(String token) {
        return jwtService.isTokenValid(token);
    }

    public void logout(HttpServletResponse response) {
        Cookie cookie = jwtService.createCleanCookie();
        response.addCookie(cookie);
    }
}