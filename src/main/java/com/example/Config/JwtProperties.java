package com.example.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Validação de propriedades JWT para garantir segurança em production.
 * Falha rápido se o segredo não estiver configurado adequadamente.
 */
@Component
public class JwtProperties {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private long expiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    @PostConstruct
    public void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "ERRO DE SEGURANÇA: jwt.secret não está configurado. " +
                "Configure a variável de ambiente JWT_SECRET com pelo menos 32 caracteres."
            );
        }

        if (secret.length() < 32) {
            throw new IllegalStateException(
                "ERRO DE SEGURANÇA: jwt.secret deve ter pelo menos 32 caracteres. " +
                "Recebido: " + secret.length() + " caracteres."
            );
        }

        if (expiration <= 0) {
            throw new IllegalStateException(
                "ERRO DE SEGURANÇA: jwt.expiration deve ser positivo (em ms)."
            );
        }

        if (refreshExpiration <= 0) {
            throw new IllegalStateException(
                "ERRO DE SEGURANÇA: jwt.refresh-expiration deve ser positivo (em ms)."
            );
        }

        System.out.println(">>> JWT Properties validadas com sucesso.");
        System.out.println("    - Secret configurado: " + (secret.length() >= 32 ? "✓" : "✗"));
        System.out.println("    - Access Token TTL: " + (expiration / 1000) + " segundos");
        System.out.println("    - Refresh Token TTL: " + (refreshExpiration / 1000) + " segundos");
    }

    public String getSecret() {
        return secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }
}
