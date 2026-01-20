package com.bvas.bvas.repository;

import com.bvas.bvas.model.DigitalSignature;
import com.bvas.bvas.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DigitalSignatureRepository extends JpaRepository<DigitalSignature, Long> {
    
    Optional<DigitalSignature> findByBill(Bill bill);
    
    boolean existsByBill(Bill bill);
}