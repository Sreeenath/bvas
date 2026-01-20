package com.bvas.bvas.model;

import com.bvas.bvas.model.enums.BillStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Bill extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;
    
    @Column(nullable = false)
    private Integer billMonth; // 1-12
    
    @Column(nullable = false)
    private Integer billYear;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BillStatus status = BillStatus.DRAFT;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillItem> billItems = new ArrayList<>();
    
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillDocument> documents = new ArrayList<>();
    
    @OneToOne(mappedBy = "bill", cascade = CascadeType.ALL)
    private DigitalSignature digitalSignature;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy; // District verifier
    
    private String rejectionRemarks;
    
    private LocalDateTime submittedAt;
    
    private LocalDateTime verifiedAt;
    
    @Column(length = 1000)
    private String vendorRemarks; // Additional notes from vendor
    
    @Transient
    public YearMonth getBillPeriod() {
        return YearMonth.of(billYear, billMonth);
    }
}