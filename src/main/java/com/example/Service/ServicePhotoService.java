package com.example.Service;

import com.example.Models.ServiceOrder;
import com.example.Models.ServicePhoto;
import com.example.Models.Usuario;
import com.example.Repository.ServiceOrderRepository;
import com.example.Repository.ServicePhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

// Serviço para upload e gerenciamento de fotos de serviço
@Service
@RequiredArgsConstructor
public class ServicePhotoService {

    private final ServicePhotoRepository servicePhotoRepository;
    private final ServiceOrderRepository serviceOrderRepository;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    // Tipos MIME permitidos para fotos
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Transactional(readOnly = true)
    public List<ServicePhoto> getPhotosByServiceOrderId(Long serviceOrderId) {
        validateOsOwnership(serviceOrderId);
        return servicePhotoRepository.findByServiceOrderId(serviceOrderId);
    }

    @Transactional
    public ServicePhoto uploadPhoto(Long serviceOrderId, MultipartFile file) throws IOException {
        validateOsOwnership(serviceOrderId);
        // Valida o tipo do arquivo
        if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Tipo de arquivo não permitido. Envie imagens (JPEG, PNG, GIF, WebP).");
        }

        ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + serviceOrderId));

        // Validação de workflow: permite fotos em qualquer estado, exceto se já estiver PAGO.
        if ("PAGO".equals(order.getStatus())) {
            throw new RuntimeException("Fotos não podem ser enviadas para uma OS PAGA");
        }

        // Sanitiza o nome do arquivo e gera nome único
        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null) {
            originalFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        }
        String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;

        // Cria o diretório de upload se não existir
        Path uploadPath = Paths.get(uploadDir, "os_" + serviceOrderId);
        Files.createDirectories(uploadPath);

        // Salva o arquivo
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Registra no banco
        ServicePhoto photo = ServicePhoto.builder()
                .serviceOrder(order)
                .fileName(originalFileName)
                .filePath(filePath.toString())
                .build();

        return servicePhotoRepository.save(photo);
    }

    @Transactional
    public void deletePhoto(Long photoId, Long serviceOrderId) throws IOException {
        validateOsOwnership(serviceOrderId);
        
        ServicePhoto photo = servicePhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada com id " + photoId));
        
        // SEGURANÇA CRÍTICA (IDOR Fix): Validar que a foto pertence à OS informada
        ServiceOrder photoOrder = photo.getServiceOrder();
        if (!photoOrder.getId().equals(serviceOrderId)) {
            throw new RuntimeException("Acesso negado: Esta foto não pertence à Ordem de Serviço especificada.");
        }
        
        // Validação de workflow: permite exclusão de fotos em qualquer estado, exceto se já estiver PAGO.
        if ("PAGO".equals(photoOrder.getStatus())) {
            throw new RuntimeException("Fotos não podem ser excluídas de uma OS PAGA");
        }

        // Remove o arquivo do disco
        Path filePath = Paths.get(photo.getFilePath());
        Files.deleteIfExists(filePath);

        servicePhotoRepository.deleteById(photoId);
    }

    // Retorna o caminho do arquivo para download/exibição
    public Path getPhotoPath(Long photoId) {
        ServicePhoto photo = servicePhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada com id " + photoId));
        
        validateOsOwnership(photo.getServiceOrder().getId());
        return Paths.get(photo.getFilePath());
    }

    private void validateOsOwnership(Long serviceOrderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        
        Usuario user = (Usuario) auth.getPrincipal();
        
        if ("TECNICO".equals(user.getRole())) {
            ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                    .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));
            
            if (order.getTechnician() == null || !order.getTechnician().getId().equals(user.getId())) {
                throw new AccessDeniedException("Você não tem permissão para acessar fotos desta Ordem de Serviço");
            }
        }
    }
}
