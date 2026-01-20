/**
 * @file VerifyOtpAndResetPasswordRequest.java
 * @company Techversant Infotech
 * @author Sreenath M
 * @date 1/19/2026
 * @version 1.0
 * @description
 */
package com.bvas.bvas.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyOtpAndResetPasswordRequest {
    
    @NotBlank(message = "Username or mobile number is required")
    private String usernameOrMobile;
    
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otp;
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
