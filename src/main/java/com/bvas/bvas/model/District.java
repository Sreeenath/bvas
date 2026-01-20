package com.bvas.bvas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "districts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class District extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 50)
    private String code; // e.g., "ALM", "BAG", etc.
    
    @Column(nullable = false, length = 100)
    private String name; // e.g., "Almora", "Bageshwar", etc.
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private Boolean isActive = true;
}