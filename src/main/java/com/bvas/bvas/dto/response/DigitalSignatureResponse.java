package com.bvas.bvas.dto.response;

import com.bvas.bvas.model.enums.SignatureType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DigitalSignatureResponse {
    private Long id;
    private SignatureType signatureType;
    private String signedDocumentPath;
    private String documentHash;
    private String signedDocumentHash;
    private String signerName;
    private String certificateSerialNumber;
    private String certificateIssuer;
    private LocalDateTime certificateValidFrom;
    private LocalDateTime certificateValidTo;
    private LocalDateTime signedAt;
    private String signerIpAddress;
    private String timestampAuthorityUrl;
    private Boolean isPadesCompliant;
    private Boolean isLongTermValidation;
    private Boolean isValid;
}