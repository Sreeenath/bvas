package com.bvas.bvas.repository;

import com.bvas.bvas.model.User;
import com.bvas.bvas.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByRoleAndIsActiveTrue(UserRole role);
    
    List<User> findByRoleAndIsApprovedFalse(UserRole role);
    
    @Query("SELECT u FROM User u JOIN u.assignedDistricts d WHERE d.id = :districtId AND u.role = :role AND u.isActive = true")
    List<User> findByDistrictAndRole(@Param("districtId") Long districtId, @Param("role") UserRole role);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
  
    Optional<User> findByMobileNumber(String mobileNumber);
}