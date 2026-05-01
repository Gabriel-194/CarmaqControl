package com.example.Config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException e) {
        System.err.println("Access Denied Error: " + e.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Acesso Negado: Você não tem permissão para esta ação.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Erro de validação nos campos informados.");
        response.put("errors", errors);
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        String message = "Erro de integridade de dados.";
        if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
            message = "Já existe um registro com estes dados (Email ou Nome duplicado).";
        }
        response.put("message", message);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        // Log detalhado internamente (seguro)
        System.err.println("Unexpected Error: " + e.getMessage());
        e.printStackTrace();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        // SEGURANÇA: Mensagem genérica para o cliente, ocultando detalhes técnicos
        response.put("message", "Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
