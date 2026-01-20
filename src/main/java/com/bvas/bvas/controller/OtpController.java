package com.bvas.bvas.controller;

import com.bvas.bvas.dto.response.ApiResponse;
import com.bvas.bvas.service.DummyOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class OtpController {

    private final DummyOtpService otpService;

    /**
     * Generate OTP for testing (DUMMY - local development only)
     * In production, OTPs should be sent automatically via SMS/Email
     */
    @PostMapping("/otp/generate")
    public ResponseEntity<ApiResponse<String>> generateOtp(@RequestParam String identifier) {
        String otp = otpService.generateOtp(identifier);
        return ResponseEntity.ok(ApiResponse.success(
            "OTP generated (DUMMY MODE). Use this OTP: " + otp, otp));
    }

    /**
     * Generate Aadhaar OTP for testing (DUMMY - local development only)
     */
    @PostMapping("/otp/aadhaar/generate")
    public ResponseEntity<ApiResponse<String>> generateAadhaarOtp(@RequestParam String aadhaarNumber) {
        String otp = otpService.generateAadhaarOtp(aadhaarNumber);
        return ResponseEntity.ok(ApiResponse.success(
            "Aadhaar OTP generated (DUMMY MODE). Use this OTP: " + otp, otp));
    }
}