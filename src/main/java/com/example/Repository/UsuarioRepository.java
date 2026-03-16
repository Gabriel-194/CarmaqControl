package com.example.Repository;

import com.example.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    // Contagem de usuários por role (para dashboard)
    long countByRole(String role);

    // Buscar usuários ativos por role (para listar técnicos)
    List<Usuario> findByRoleAndAtivoTrue(String role);
}