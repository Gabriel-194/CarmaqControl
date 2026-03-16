package com.example.Repository;

import com.example.Models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    List<Client> findAllByActiveTrue();
    
    List<Client> findByCompanyNameContainingIgnoreCaseAndActiveTrue(String companyName);
}
