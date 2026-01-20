package com.bvas.bvas.service;

import com.bvas.bvas.dto.request.DigitalSignatureRequest;
import com.bvas.bvas.exception.*;
import com.bvas.bvas.model.Bill;
import com.bvas.bvas.model.DigitalSignature;
import com.bvas.bvas.model.User;
import com.bvas.bvas.model.enums.BillStatus;
import com.bvas.bvas.model.enums.SignatureType;
import com.bvas.bvas.repository.BillRepository;
import com.bvas.bvas.repository.DigitalSignatureRepository;
import com.bvas.bvas.service.DummyAadhaarESignService.AadhaarESignResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalSignatureService {

    private final DigitalSignatureRepository signatureRepository;
    private final BillRepository billRepository;
    private final AuditLogService auditLogService;
    private final FileStorageService fileStorageService;
    private final DummyAadhaarESignService aadhaarESignService;
    private final DummyOtpService otpService;

    @Transactional
    public DigitalSignature signBill(DigitalSignatureRequest request, User signer, String ipAddress) {
        Bill bill = billRepository.findById(request.getBillId())
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + request.getBillId()));

        if (bill.getStatus() != BillStatus.PENDING_DISTRICT_VERIFICATION) {
            throw new InvalidBillStatusException("Bill must be in pending status for signing. Current status: " + bill.getStatus());
        }

        SignatureType signatureType = SignatureType.valueOf(request.getSignatureType());
        String signedDocumentPath;
        String documentHash = calculateDocumentHash(bill);
        
        // Handle different signature types
        if (signatureType == SignatureType.AADHAAR_ESIGN) {
            signedDocumentPath = performAadhaarESign(bill, request, documentHash, signer);
        } else {
            signedDocumentPath = performDscSigning(bill, request, signatureType);
        }
        
        // Create signature record
        DigitalSignature signature = new DigitalSignature();
        signature.setBill(bill);
        signature.setSignatureType(signatureType);
        signature.setSignedDocumentPath(signedDocumentPath);
        signature.setDocumentHash(documentHash);
        signature.setSignedDocumentHash(calculateSignedDocumentHash(signedDocumentPath));
        signature.setSignerName(signer.getFullName());
        signature.setCertificateSerialNumber(extractCertificateSerial(request, signatureType));
        signature.setCertificateIssuer(extractCertificateIssuer(request, signatureType));
        signature.setCertificateValidFrom(LocalDateTime.now());
        signature.setCertificateValidTo(LocalDateTime.now().plusYears(2));
        signature.setSignedAt(LocalDateTime.now());
        signature.setSignerIpAddress(ipAddress);
        signature.setIsPadesCompliant(true);
        signature.setIsLongTermValidation(true);
        signature.setIsValid(true);

        signature = signatureRepository.save(signature);

        // Update bill status
        bill.setStatus(BillStatus.APPROVED);
        bill.setVerifiedBy(signer);
        bill.setVerifiedAt(LocalDateTime.now());
        billRepository.save(bill);

        // Audit log
        auditLogService.logDigitalSignature(bill, signer, ipAddress);

        return signature;
    }

    private String performAadhaarESign(Bill bill, DigitalSignatureRequest request, 
                                       String documentHash, User signer) {
        log.info("DUMMY MODE: Performing Aadhaar eSign for bill ID: {}", bill.getId());
        
        // Validate Aadhaar number format (12 digits)
        String aadhaarNumber = request.getAadhaarNumber();
        if (aadhaarNumber == null || !aadhaarNumber.matches("^[0-9]{12}$")) {
            throw new InvalidAadhaarException("Invalid Aadhaar number format. Must be 12 digits.");
        }

        // Initiate eSign
        DummyAadhaarESignService.AadhaarESignResponse response = 
            aadhaarESignService.initiateESign(aadhaarNumber, documentHash);

        // Validate OTP
        if (request.getOtp() == null || request.getOtp().isEmpty()) {
            throw new InvalidOtpException("OTP is required for Aadhaar eSign");
        }

        boolean otpValid = otpService.validateAadhaarOtp(aadhaarNumber, request.getOtp());
        if (!otpValid) {
            String defaultOtp = otpService.generateAadhaarOtp(aadhaarNumber);
            throw new InvalidOtpException("Invalid OTP. Please use the default OTP: " + defaultOtp);
        }

        // Complete eSign
        AadhaarESignResult result = aadhaarESignService.verifyOtpAndSign(
            response.getTransactionId(), aadhaarNumber, request.getOtp(), documentHash);

        if (!result.getSuccess()) {
            throw new DigitalSignatureException("Aadhaar eSign failed");
        }

        // Store signed document (in dummy mode, just create a placeholder path)
        String signedPath = "signed/aadhaar_" + bill.getId() + "_" + System.currentTimeMillis() + ".pdf";
        
        log.warn("DUMMY MODE: Aadhaar eSign completed successfully. In production, actual signed PDF would be stored.");
        
        return signedPath;
    }

    private String performDscSigning(Bill bill, DigitalSignatureRequest request, SignatureType signatureType) {
        log.info("DUMMY MODE: Performing DSC signing for bill ID: {}, type: {}", bill.getId(), signatureType);
        
        if (signatureType == SignatureType.CLASS3_DSC_PFX) {
            if (request.getCertificatePassword() == null || request.getCertificatePassword().isEmpty()) {
                throw new BadRequestException("Certificate password is required for PFX file");
            }
            log.warn("DUMMY MODE: Would load certificate from PFX file with provided password");
        } else if (signatureType == SignatureType.CLASS3_DSC_USB) {
            log.warn("DUMMY MODE: Would access USB token for certificate");
        }

        // Simulate signing delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String signedPath = "signed/dsc_" + bill.getId() + "_" + System.currentTimeMillis() + ".pdf";
        log.warn("DUMMY MODE: DSC signing completed. In production, actual signed PDF would be stored.");
        
        return signedPath;
    }

    private String calculateDocumentHash(Bill bill) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Combine bill data for hashing
            StringBuilder billData = new StringBuilder();
            billData.append(bill.getId());
            billData.append(bill.getBillMonth());
            billData.append(bill.getBillYear());
            billData.append(bill.getTotalAmount());
            billData.append(bill.getStatus());
            
            // Add bill items
            if (bill.getBillItems() != null) {
                billData.append(bill.getBillItems().stream()
                    .map(item -> item.getDistrict().getId() + ":" + item.getQuantity())
                    .collect(Collectors.joining(",")));
            }
            
            // Add document hashes if available
            if (bill.getDocuments() != null) {
                billData.append(bill.getDocuments().stream()
                    .map(doc -> doc.getFileHash() != null ? doc.getFileHash() : "")
                    .collect(Collectors.joining(",")));
            }
            
            byte[] hash = digest.digest(billData.toString().getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to calculate document hash", e);
            return "hash_" + bill.getId() + "_" + System.currentTimeMillis();
        }
    }

    private String calculateSignedDocumentHash(String signedDocumentPath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Try to read the file if it exists
            try {
                byte[] fileBytes = Files.readAllBytes(Paths.get(signedDocumentPath));
                byte[] hash = digest.digest(fileBytes);
                return bytesToHex(hash);
            } catch (IOException e) {
                // If file doesn't exist (dummy mode), create hash from path
                byte[] hash = digest.digest(signedDocumentPath.getBytes());
                return bytesToHex(hash);
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to calculate signed document hash", e);
            return "signed_hash_" + System.currentTimeMillis();
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private String extractCertificateSerial(DigitalSignatureRequest request, SignatureType signatureType) {
        if (signatureType == SignatureType.AADHAAR_ESIGN) {
            return "DUMMY_AADHAAR_CERT_" + System.currentTimeMillis();
        }
        return "DUMMY_DSC_CERT_" + System.currentTimeMillis();
    }

    private String extractCertificateIssuer(DigitalSignatureRequest request, SignatureType signatureType) {
        if (signatureType == SignatureType.AADHAAR_ESIGN) {
            return "DUMMY_CA_INDIA_AADHAAR";
        }
        return "DUMMY_CA_INDIA_DSC";
    }
}