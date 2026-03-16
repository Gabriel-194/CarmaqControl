package com.example.Service;

import com.example.Models.ServiceOrder;
import com.example.Models.ServicePhoto;
import com.example.Repository.ServiceOrderRepository;
import com.example.Repository.ServicePhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
        return servicePhotoRepository.findByServiceOrderId(serviceOrderId);
    }

    @Transactional
    public ServicePhoto uploadPhoto(Long serviceOrderId, MultipartFile file) throws IOException {
        // Valida o tipo do arquivo
        if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Tipo de arquivo não permitido. Envie imagens (JPEG, PNG, GIF, WebP).");
        }

        ServiceOrder order = serviceOrderRepository.findById(serviceOrderId)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada com id " + serviceOrderId));

        // Validação de workflow: só permite upload de fotos se EM_ANDAMENTO
        if (!"EM_ANDAMENTO".equals(order.getStatus())) {
            throw new RuntimeException("Fotos só podem ser enviadas quando a OS está EM_ANDAMENTO");
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
    public void deletePhoto(Long photoId) throws IOException {
        ServicePhoto photo = servicePhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada com id " + photoId));

        // Remove o arquivo do disco
        Path filePath = Paths.get(photo.getFilePath());
        Files.deleteIfExists(filePath);

        servicePhotoRepository.deleteById(photoId);
    }

    // Retorna o caminho do arquivo para download/exibição
    public Path getPhotoPath(Long photoId) {
        ServicePhoto photo = servicePhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada com id " + photoId));
        return Paths.get(photo.getFilePath());
    }
}
