package com.example.Controller;

import com.example.Models.ServicePhoto;
import com.example.Service.ServicePhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Controller para upload e gerenciamento de fotos de serviço
@RestController
@RequestMapping("/api/service-orders/{serviceOrderId}/photos")
@RequiredArgsConstructor
public class ServicePhotoController {

    private final ServicePhotoService servicePhotoService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<List<Map<String, Object>>> getPhotos(@PathVariable(name = "serviceOrderId") Long serviceOrderId) {
        List<ServicePhoto> photos = servicePhotoService.getPhotosByServiceOrderId(serviceOrderId);
        // Retorna apenas metadados (sem o caminho do arquivo no servidor)
        List<Map<String, Object>> response = photos.stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "fileName", p.getFileName(),
                        "uploadedAt", p.getUploadedAt().toString()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<Map<String, Object>> uploadPhoto(
            @PathVariable(name = "serviceOrderId") Long serviceOrderId,
            @RequestParam(name = "file") MultipartFile file) throws IOException {
        ServicePhoto photo = servicePhotoService.uploadPhoto(serviceOrderId, file);
        Map<String, Object> response = Map.of(
                "id", photo.getId(),
                "fileName", photo.getFileName(),
                "uploadedAt", photo.getUploadedAt().toString()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Endpoint para exibir a imagem diretamente
    @GetMapping("/{photoId}/view")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")
    public ResponseEntity<Resource> viewPhoto(
            @PathVariable(name = "serviceOrderId") Long serviceOrderId, @PathVariable(name = "photoId") Long photoId) throws IOException {
        Path filePath = servicePhotoService.getPhotoPath(photoId);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @DeleteMapping("/{photoId}")
    @PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'TECNICO')")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable(name = "serviceOrderId") Long serviceOrderId, @PathVariable(name = "photoId") Long photoId) throws IOException {
        servicePhotoService.deletePhoto(photoId);
        return ResponseEntity.noContent().build();
    }
}
