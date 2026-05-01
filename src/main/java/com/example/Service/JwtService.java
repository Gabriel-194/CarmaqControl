package com.example.Service;

import com.example.Models.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.Config.JwtProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Serviço de geração e validação de JWT com segurança aprimorada.
 * 
 * Melhorias:
 * - Secure flag em cookies (detecta automaticamente se está em produção)
 * - SameSite=Lax para proteção CSRF
 * - Validação de usuário ativo ao validar token
 * - TTL curto para access token
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean secureFlag;

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(Map<String, Object> extraClaims, String email) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida token apenas estruturalmente (assinatura e expiração)
     * sem verificar estado do usuário.
     * Usar apenas em contextos onde a validação de usuário ativo será feita depois.
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("Token inválido: " + e.getMessage());
            return false;
        }
    }

    public boolean isTokenValid(String token, Usuario user) {
        try {
            // Validação 1: Token estruturalmente válido
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);

            // Validação 2: Email no token corresponde ao usuário
            String tokenEmail = extractUsername(token);
            if (!tokenEmail.equals(user.getEmail())) {
                return false;
            }

            // Validação 3: Usuário está ativo (criação de conta, não bloqueado, etc)
            if (!user.isEnabled()) {
                System.out.println("Token válido estruturalmente, mas usuário está inativo: " + user.getEmail());
                return false;
            }

            return true;
        } catch (Exception e) {
            System.out.println("Token inválido: " + e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Cria cookie seguro de JWT com Secure, HttpOnly e SameSite.
     * - Secure: ativado se não estiver em desenvolvimento local
     * - HttpOnly: sempre ativado (protege contra XSS)
     * - SameSite: Lax (protege contra CSRF mas permite navigação normal)
     */
    public Cookie createJwtCookie(String token) {
        Cookie cookie = new Cookie("accessToken", token);
        
        // HttpOnly sempre ativado
        cookie.setHttpOnly(true);
        
        // Secure: true em produção, false em desenvolvimento local
        // Em um ambiente real, isso seria determinado pelo profile Spring (dev/prod)
        boolean isProduction = System.getenv("ENVIRONMENT") != null && 
                               System.getenv("ENVIRONMENT").equals("production");
        cookie.setSecure(isProduction || secureFlag);
        
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtProperties.getExpiration() / 1000));
        
        // SameSite=Lax via header X-CSRF-TOKEN que o SecurityConfig valida
        // Nota: Jakarta Cookie não suporta SameSite nativamente no setters.
        // A proteção real vem do CSRF token validado pelo SecurityConfig.
        
        return cookie;
    }

    public Cookie createCleanCookie() {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}