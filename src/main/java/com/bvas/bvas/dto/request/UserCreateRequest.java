package com.bvas.bvas.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Data
public class UserCreateRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8)
    private String password;
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;
    
    @Email
    @Size(max = 100)
    private String email;
    
    @Pattern(regexp = "^[0-9]{10}$")
    private String mobileNumber;
    
    @NotBlank(message = "Role is required")
    private String role; // "VENDOR", "DISTRICT_VERIFIER", "HQ_ADMIN"
    
    private List<Long> districtIds; // For DISTRICT_VERIFIER role
    
    private Boolean isActive = true;
}