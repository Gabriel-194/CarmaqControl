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

import java.time.LocalDateTime;
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

        // 2. Verifica se a conta está bloqueada
        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            return new LoginResponseDTO(false, "Conta bloqueada por excesso de tentativas falhas. Tente novamente mais tarde.", null, null, null, null);
        }

        if (!user.getAtivo()) {
            return new LoginResponseDTO(false, "Usuário desativado", null, null, null, null);
        }

        // 3. Verifica a senha
        if (!passwordEncoder.matches(dto.getSenha(), user.getSenha())) {
            int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() + 1 : 1;
            user.setFailedLoginAttempts(attempts);
            
            if (attempts >= 5) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(15));
                usuarioRepository.save(user);
                return new LoginResponseDTO(false, "Conta bloqueada por 15 minutos devido a múltiplas tentativas falhas.", null, null, null, null);
            }
            
            usuarioRepository.save(user);
            return new LoginResponseDTO(false, "Email ou senha incorretos", null, null, null, null);
        }

        // 4. Login bem-sucedido: reseta tentativas e limpa bloqueio
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        usuarioRepository.save(user);

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