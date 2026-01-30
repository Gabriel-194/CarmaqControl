package com.example.Config;

import com.example.Service.JwtService;
import com.example.Service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = null;

        // Tenta pegar o token dos Cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // Valida o Token
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // BLINDAGEM: O try-catch evita que um token ruim quebre a requisição
                String userEmail = jwtService.extractUsername(token);

                if (userEmail != null) {
                    // Carrega detalhes apenas para preencher o contexto
                    // Se der erro aqui, cai no catch e a requisição segue como "não autenticada"
                    var userDetails = this.usuarioService.loadUserByUsername(userEmail);

                    if (jwtService.isTokenValid(token)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // Se o token estiver estragado, apenas ignoramos.
                // O usuário continuará como "não logado" e o Controller decide se deixa passar ou dá 403.
                // Isso conserta o problema do /auth/login dar 403.
            }
        }

        filterChain.doFilter(request, response);
    }
}