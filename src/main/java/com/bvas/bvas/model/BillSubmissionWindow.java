package com.bvas.bvas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "bill_submission_windows")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BillSubmissionWindow extends BaseEntity {
    
    @Column(nullable = false)
    private Integer month; // 1-12
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(nullable = false)
    private Boolean isLocked = false; // If true, vendors cannot submit bills for this month
    
    @Column(length = 500)
    private String lockReason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by")
    private User lockedBy; // HQ admin who locked it
    
    private LocalDateTime lockedAt;
    
    private LocalDateTime unlockedAt;
    
    @Column(length = 1000)
    private String notes; // Additional notes about the submission window
    
    @Transient
    public YearMonth getPeriod() {
        return YearMonth.of(year, month);
    }
}