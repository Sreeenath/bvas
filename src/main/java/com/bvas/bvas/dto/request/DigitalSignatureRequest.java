package com.bvas.bvas.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DigitalSignatureRequest {
    
    @NotNull(message = "Bill ID is required")
    private Long billId;
    
    @NotNull(message = "Signature type is required")
    private String signatureType; // "CLASS3_DSC_USB", "CLASS3_DSC_PFX", "AADHAAR_ESIGN"
    
    private String certificatePassword; // For .pfx/.p12 files
    
    @Pattern(regexp = "^\\d{12}$", message = "Aadhaar number must be 12 digits")
    private String aadhaarNumber; // For Aadhaar eSign
    
    private String otp; // For Aadhaar eSign OTP
    
    @NotNull(message = "Re-authentication password is required")
    private String reAuthPassword;
}