package com.bvas.bvas.model;

import com.bvas.bvas.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, length = 100)
    private String fullName;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(length = 10)
    private String mobileNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_districts",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "district_id")
    )
    private Set<District> assignedDistricts = new HashSet<>();
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Boolean isApproved = false; // For vendor self-registration
    
    private Boolean twoFactorEnabled = false;
    
    private String twoFactorSecret;
    
    private LocalDateTime lastLoginAt;
    
    private String createdBy; // HQ admin who created this user
    
    @Column(length = 500)
    private String remarks; // For rejection/blocking reasons
}