package com.example.Config;

import com.example.Service.JwtService;
import com.example.Service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
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
        } else {
            System.out.println("JWT Filter: No cookies found for request " + request.getRequestURI());
        }

        // Valida o Token
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String userEmail = jwtService.extractUsername(token);

                if (userEmail != null) {
                    var userDetails = this.usuarioService.loadUserByUsername(userEmail);

                    if (jwtService.isTokenValid(token)) {
                        System.out.println("JWT Filter: Authenticating user: " + userEmail + " with roles: " + userDetails.getAuthorities());
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        System.out.println("JWT Filter: Token invalid for user: " + userEmail);
                    }
                } else {
                    System.out.println("JWT Filter: Could not extract email from token");
                }
            } catch (Exception e) {
                System.err.println("JWT Filter Error: " + e.getMessage());
            }
        } else if (token == null) {
            // Ignora favicon e auth endpoints para não poluir logs demais
            if (!request.getRequestURI().contains("favicon") && !request.getRequestURI().contains("/api/auth/")) {
                System.out.println("JWT Filter: Token is null for " + request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }
}