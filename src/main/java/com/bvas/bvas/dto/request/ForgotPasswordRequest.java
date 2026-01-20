package com.bvas.bvas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    
    @NotBlank(message = "Username or mobile number is required")
    private String usernameOrMobile;
}




