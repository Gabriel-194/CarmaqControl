package com.example.Repository;

import com.example.Models.ServicePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repositório para fotos de serviço
@Repository
public interface ServicePhotoRepository extends JpaRepository<ServicePhoto, Long> {

    // Buscar todas as fotos de uma OS
    List<ServicePhoto> findByServiceOrderId(Long serviceOrderId);
}
