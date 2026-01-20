package com.bvas.bvas.dto.response;

import com.bvas.bvas.model.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String mobileNumber;
    private UserRole role;
    private List<DistrictResponse> assignedDistricts;
    private Boolean isActive;
    private Boolean isApproved;
    private Boolean twoFactorEnabled;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}