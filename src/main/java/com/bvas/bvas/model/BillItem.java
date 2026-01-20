package com.bvas.bvas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_items")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BillItem extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity; // Distribution quantity
    
    @Column(precision = 10, scale = 2)
    private BigDecimal eposQuantity; // From ePOS system for comparison
    
    @Column(nullable = false)
    private Boolean hasDiscrepancy = false; // Auto-calculated
    
    @Column(length = 500)
    private String discrepancyNotes; // Auto-generated or manual
    
    @Column(precision = 15, scale = 2)
    private BigDecimal amount; // Calculated amount for this district
}