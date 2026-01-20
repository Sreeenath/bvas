package com.bvas.bvas.model;

import com.bvas.bvas.model.enums.SignatureType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "digital_signatures")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DigitalSignature extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false, unique = true)
    private Bill bill;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SignatureType signatureType;
    
    @Column(nullable = false, length = 500)
    private String signedDocumentPath; // Path to signed PDF
    
    @Column(nullable = false, length = 64)
    private String documentHash; // SHA-256 hash before signing
    
    @Column(nullable = false, length = 64)
    private String signedDocumentHash; // SHA-256 hash after signing
    
    @Column(nullable = false, length = 100)
    private String signerName;
    
    @Column(nullable = false, length = 200)
    private String certificateSerialNumber;
    
    @Column(nullable = false, length = 500)
    private String certificateIssuer;
    
    @Column(nullable = false)
    private LocalDateTime certificateValidFrom;
    
    @Column(nullable = false)
    private LocalDateTime certificateValidTo;
    
    @Column(nullable = false)
    private LocalDateTime signedAt;
    
    @Column(nullable = false, length = 45)
    private String signerIpAddress;
    
    @Column(length = 500)
    private String timestampAuthorityUrl; // TSA URL if used
    
    @Column(nullable = false)
    private Boolean isPadesCompliant = true;
    
    @Column(nullable = false)
    private Boolean isLongTermValidation = false; // B-LT signature
    
    @Column(length = 1000)
    private String signatureMetadata; // JSON string with additional metadata
    
    @Column(nullable = false)
    private Boolean isValid = true; // Certificate validation result
}