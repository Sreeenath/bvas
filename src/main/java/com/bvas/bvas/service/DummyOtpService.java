package com.bvas.bvas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PostConstruct;

/**
 * DUMMY OTP Service for Local Development Only
 * This service simulates OTP generation and validation without actual SMS/Email integration.
 * In production, this should be replaced with actual OTP provider (e.g., Twilio, AWS SNS, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DummyOtpService {

    @Value("${otp.default:123456}")
    private String defaultOtp;

    @Value("${otp.expiry.seconds:300}")
    private Integer otpExpirySeconds;

    @PostConstruct
    public void init() {
        log.info("DummyOtpService initialized with default OTP: '{}', expiry: {} seconds", 
            defaultOtp, otpExpirySeconds);
        if (defaultOtp == null || defaultOtp.isEmpty() || defaultOtp.length() != 6) {
            log.warn("Default OTP is not configured correctly. Will generate random OTPs.");
        }
    }

    // In-memory storage for OTPs (use Redis in production)
    private final Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    /**
     * Generate OTP for a user/mobile number
     * In local development, returns the default OTP from properties
     */
    public String generateOtp(String identifier) {
        String otp;
        
        // Use default OTP if configured, otherwise generate random 6-digit OTP
        if (defaultOtp != null && !defaultOtp.isEmpty() && defaultOtp.length() == 6) {
            otp = defaultOtp;
            log.info("DUMMY OTP: Using default OTP '{}' for identifier: {}", otp, identifier);
        } else {
            otp = generateRandomOtp();
            log.info("DUMMY OTP: Generated random OTP '{}' for identifier: {}", otp, identifier);
        }

        // Store OTP with expiry
        OtpData otpData = new OtpData(otp, LocalDateTime.now().plusSeconds(otpExpirySeconds));
        otpStore.put(identifier, otpData);

        log.warn("DUMMY MODE: OTP '{}' generated for {}. In production, this would be sent via SMS/Email.", 
            otp, identifier);
        
        return otp;
    }

    /**
     * Validate OTP
     */
    public boolean validateOtp(String identifier, String otp) {
        OtpData otpData = otpStore.get(identifier);
        
        if (otpData == null) {
            log.warn("OTP validation failed: No OTP found for identifier: {}", identifier);
            return false;
        }

        if (otpData.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("OTP validation failed: OTP expired for identifier: {}", identifier);
            otpStore.remove(identifier);
            return false;
        }

        boolean isValid = otpData.getOtp().equals(otp);
        
        if (isValid) {
            log.info("DUMMY OTP: OTP validated successfully for identifier: {}", identifier);
            otpStore.remove(identifier); // Remove after successful validation
        } else {
            log.warn("OTP validation failed: Invalid OTP for identifier: {}", identifier);
        }

        return isValid;
    }

    /**
     * Generate Aadhaar OTP (same logic, but for Aadhaar context)
     */
    public String generateAadhaarOtp(String aadhaarNumber) {
        log.info("DUMMY Aadhaar OTP: Generating OTP for Aadhaar number: {}", maskAadhaar(aadhaarNumber));
        return generateOtp("AADHAAR_" + aadhaarNumber);
    }

    /**
     * Validate Aadhaar OTP
     */
    public boolean validateAadhaarOtp(String aadhaarNumber, String otp) {
        log.info("DUMMY Aadhaar OTP: Validating OTP for Aadhaar number: {}", maskAadhaar(aadhaarNumber));
        return validateOtp("AADHAAR_" + aadhaarNumber, otp);
    }

    /**
     * Generate random 6-digit OTP
     */
    private String generateRandomOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Mask Aadhaar number for logging (show only last 4 digits)
     */
    private String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) {
            return "****";
        }
        return "****" + aadhaar.substring(aadhaar.length() - 4);
    }

    /**
     * Clear expired OTPs (cleanup method)
     */
    public void clearExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        otpStore.entrySet().removeIf(entry -> entry.getValue().getExpiryTime().isBefore(now));
    }

    /**
     * Inner class to store OTP data with expiry
     */
    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;

        public OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }
}