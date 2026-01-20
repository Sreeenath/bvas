package com.bvas.bvas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillApprovalRequest {
    
    @NotNull(message = "Bill ID is required")
    private Long billId;
    
    @NotBlank(message = "Action is required (APPROVE or REJECT)")
    private String action; // "APPROVE" or "REJECT"
    
    private String remarks; // Mandatory for REJECT, optional for APPROVE
    
    @NotBlank(message = "Re-authentication password is required")
    private String reAuthPassword; // For re-authentication before signing
}