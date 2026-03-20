package com.example.Config;

import com.example.Models.Usuario;
import com.example.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        // Se não houver nenhum usuário, cria o administrador (Proprietário) padrão
        if (usuarioRepository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .nome("Administrador Carmarq")
                    .email("admin@carmaq.com")
                    .senha(passwordEncoder.encode("admin123"))
                    .role("PROPRIETARIO")
                    .telefone("(11) 99999-9999")
                    .ativo(true)
                    .build();

            usuarioRepository.save(admin);
            System.out.println(">>> Usuário Administrador padrão criado: admin@carmaq.com / admin123");
        }

        // Criar usuário específico solicitado pelo usuário
        if (usuarioRepository.findByEmail("proprietario@empresa.com").isEmpty()) {
            Usuario proprietario = Usuario.builder()
                    .nome("Proprietário Empresa")
                    .email("proprietario@empresa.com")
                    .senha(passwordEncoder.encode("123123"))
                    .role("PROPRIETARIO")
                    .telefone("(11) 98888-8888")
                    .ativo(true)
                    .build();

            usuarioRepository.save(proprietario);
            System.out.println(">>> Usuário Proprietário específico criado: proprietario@empresa.com / 123123");
        }
    }
}
