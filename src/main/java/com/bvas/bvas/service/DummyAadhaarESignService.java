package com.bvas.bvas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * DUMMY Aadhaar eSign Service for Local Development Only
 * This service simulates Aadhaar-based eSign without actual integration with licensed providers
 * (eMudhra, CDSL, Capricorn, etc.)
 * 
 * In production, this should be replaced with actual Aadhaar eSign API integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DummyAadhaarESignService {

    @Value("${aadhaar.esign.dummy.enabled:true}")
    private Boolean dummyModeEnabled;

    /**
     * Initiate Aadhaar eSign process (dummy implementation)
     * In real implementation, this would call the eSign provider API
     */
    public AadhaarESignResponse initiateESign(String aadhaarNumber, String documentHash) {
        log.warn("DUMMY MODE: Initiating Aadhaar eSign for Aadhaar: {} (masked)", maskAadhaar(aadhaarNumber));
        log.info("DUMMY MODE: Document hash: {}", documentHash);

        // Simulate API call delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generate dummy transaction ID
        String transactionId = "DUMMY_TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        AadhaarESignResponse response = new AadhaarESignResponse();
        response.setTransactionId(transactionId);
        response.setStatus("OTP_SENT");
        response.setMessage("OTP sent to registered mobile number (DUMMY)");
        response.setTimestamp(LocalDateTime.now());

        log.info("DUMMY MODE: Transaction ID generated: {}", transactionId);
        log.warn("DUMMY MODE: In production, OTP would be sent to mobile number linked with Aadhaar");

        return response;
    }

    /**
     * Verify OTP and complete eSign (dummy implementation)
     */
    public AadhaarESignResult verifyOtpAndSign(String transactionId, String aadhaarNumber, 
                                                String otp, String documentHash) {
        log.warn("DUMMY MODE: Verifying OTP and signing document");
        log.info("DUMMY MODE: Transaction ID: {}, Aadhaar: {} (masked), OTP: {}", 
            transactionId, maskAadhaar(aadhaarNumber), otp);

        // Simulate API call delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // In dummy mode, accept any OTP (or use default OTP validation)
        // In production, this would verify OTP with the eSign provider
        
        AadhaarESignResult result = new AadhaarESignResult();
        result.setSuccess(true);
        result.setTransactionId(transactionId);
        result.setSignedDocumentHash(generateDummySignedHash(documentHash));
        result.setSignatureValue(generateDummySignatureValue());
        result.setCertificateSerialNumber("DUMMY_CERT_SERIAL_" + UUID.randomUUID().toString().substring(0, 8));
        result.setCertificateIssuer("DUMMY_CA_INDIA");
        result.setSignerName("DUMMY_SIGNER_" + maskAadhaar(aadhaarNumber));
        result.setSignedAt(LocalDateTime.now());
        result.setTimestampAuthorityUrl("DUMMY_TSA_URL");
        result.setIsPadesCompliant(true);
        result.setIsLongTermValidation(true);

        log.info("DUMMY MODE: Document signed successfully");
        log.warn("DUMMY MODE: In production, this would return actual digital signature from licensed provider");

        return result;
    }

    /**
     * Generate dummy signed document hash
     */
    private String generateDummySignedHash(String originalHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = originalHash + "_SIGNED_" + System.currentTimeMillis();
            byte[] hash = digest.digest(combined.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return "DUMMY_SIGNED_HASH_" + UUID.randomUUID().toString();
        }
    }

    /**
     * Generate dummy signature value
     */
    private String generateDummySignatureValue() {
        return Base64.getEncoder().encodeToString(
            ("DUMMY_SIGNATURE_" + UUID.randomUUID() + "_" + System.currentTimeMillis()).getBytes()
        );
    }

    /**
     * Mask Aadhaar number for logging
     */
    private String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) {
            return "****";
        }
        return "****" + aadhaar.substring(aadhaar.length() - 4);
    }

    /**
     * Response DTO for eSign initiation
     */
    public static class AadhaarESignResponse {
        private String transactionId;
        private String status;
        private String message;
        private LocalDateTime timestamp;

        // Getters and setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * Result DTO for completed eSign
     */
    public static class AadhaarESignResult {
        private Boolean success;
        private String transactionId;
        private String signedDocumentHash;
        private String signatureValue;
        private String certificateSerialNumber;
        private String certificateIssuer;
        private String signerName;
        private LocalDateTime signedAt;
        private String timestampAuthorityUrl;
        private Boolean isPadesCompliant;
        private Boolean isLongTermValidation;

        // Getters and setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getSignedDocumentHash() { return signedDocumentHash; }
        public void setSignedDocumentHash(String signedDocumentHash) { this.signedDocumentHash = signedDocumentHash; }
        public String getSignatureValue() { return signatureValue; }
        public void setSignatureValue(String signatureValue) { this.signatureValue = signatureValue; }
        public String getCertificateSerialNumber() { return certificateSerialNumber; }
        public void setCertificateSerialNumber(String certificateSerialNumber) { this.certificateSerialNumber = certificateSerialNumber; }
        public String getCertificateIssuer() { return certificateIssuer; }
        public void setCertificateIssuer(String certificateIssuer) { this.certificateIssuer = certificateIssuer; }
        public String getSignerName() { return signerName; }
        public void setSignerName(String signerName) { this.signerName = signerName; }
        public LocalDateTime getSignedAt() { return signedAt; }
        public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }
        public String getTimestampAuthorityUrl() { return timestampAuthorityUrl; }
        public void setTimestampAuthorityUrl(String timestampAuthorityUrl) { this.timestampAuthorityUrl = timestampAuthorityUrl; }
        public Boolean getIsPadesCompliant() { return isPadesCompliant; }
        public void setIsPadesCompliant(Boolean isPadesCompliant) { this.isPadesCompliant = isPadesCompliant; }
        public Boolean getIsLongTermValidation() { return isLongTermValidation; }
        public void setIsLongTermValidation(Boolean isLongTermValidation) { this.isLongTermValidation = isLongTermValidation; }
    }
}