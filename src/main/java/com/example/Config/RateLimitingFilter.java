package com.example.Config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final int MAX_LOGIN_REQUESTS_PER_MINUTE = 5;
    
    // Cache de Buckets: Chave Única (IP + Tipo) -> Bucket
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String ip = getClientIp(httpRequest);
        String uri = httpRequest.getRequestURI();

        // Verifica se é estritamente uma tentativa de Login (Rota exata + Método POST)
        boolean isLogin = uri.equals("/api/auth/login") && httpRequest.getMethod().equalsIgnoreCase("POST");

        // Cria uma chave separada para Login e para uso da API
        String cacheKey = ip + (isLogin ? "_login" : "_api");

        // Obtém ou cria o bucket para esta chave
        Bucket bucket = buckets.computeIfAbsent(cacheKey, k -> createNewBucket(isLogin));

        // Tenta consumir 1 token
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            System.err.println("Rate Limit Exceeded for IP: " + ip + " on URI: " + uri);
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setContentType("text/plain; charset=UTF-8");
            httpResponse.getWriter().write("Muitas requisições. Tente novamente em um minuto.");
        }
    }

    private Bucket createNewBucket(boolean isLogin) {
        long limit = isLogin ? MAX_LOGIN_REQUESTS_PER_MINUTE : MAX_REQUESTS_PER_MINUTE;
        // Limite de 'limit' tokens por minuto, recarregando 'limit' a cada minuto usando a API moderna
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit)
                .refillGreedy(limit, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}