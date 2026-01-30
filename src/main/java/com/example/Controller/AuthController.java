package com.example.Controller;

import com.example.DTOs.LoginRequestDTO;
import com.example.DTOs.LoginResponseDTO;
import com.example.Models.Usuario;
import com.example.Repository.UsuarioRepository;
import com.example.Service.AuthService;
import com.example.Service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
        try {
            LoginResponseDTO res = authService.login(loginRequest, response);
            if (res.getSuccess()) {
                return ResponseEntity.ok(res);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponseDTO(false, "Erro interno", null, null, null, null));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {
        String token = extractToken(request);

        if (token == null || !jwtService.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
        }

        String email = jwtService.extractUsername(token);
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            Map<String, Object> response = new HashMap<>();

            // AQUI ESTÁ A CORREÇÃO:
            // Retornamos apenas o essencial para a navegação funcionar
            response.put("valid", true);
            response.put("role", user.getRole()); // Frontend precisa disso para o Dashboard
            response.put("nome", user.getNome()); // Frontend precisa disso para a Sidebar ("Olá, Fulano")

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("valid", false));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok().build();
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("accessToken".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}