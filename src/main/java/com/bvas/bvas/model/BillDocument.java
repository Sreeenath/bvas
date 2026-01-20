package com.bvas.bvas.model;

import com.bvas.bvas.model.enums.DocumentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bill_documents")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BillDocument extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentType documentType;
    
    @Column(nullable = false, length = 255)
    private String fileName;
    
    @Column(nullable = false, length = 500)
    private String filePath; // Storage path
    
    @Column(nullable = false)
    private Long fileSize; // in bytes
    
    @Column(length = 50)
    private String mimeType; // e.g., "application/pdf", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    
    @Column(length = 64)
    private String fileHash; // SHA-256 hash for integrity
    
    @Column(length = 500)
    private String description;
}