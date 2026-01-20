package com.bvas.bvas.repository;

import com.bvas.bvas.model.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    
    Optional<District> findByCode(String code);
    
    List<District> findByIsActiveTrue();
    
    boolean existsByCode(String code);
}