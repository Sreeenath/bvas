package com.bvas.bvas.model;

import com.bvas.bvas.model.enums.AuditAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User performedBy;
    
    @Column(length = 100)
    private String entityType; // e.g., "Bill", "User", "BillSubmissionWindow"
    
    @Column
    private Long entityId; // ID of the affected entity
    
    @Column(length = 45)
    private String ipAddress;
    
    @Column(length = 1000)
    private String details; // JSON string with action details
    
    @Column(length = 2000)
    private String remarks;
    
    @Column(nullable = false)
    private LocalDateTime actionTimestamp;
}