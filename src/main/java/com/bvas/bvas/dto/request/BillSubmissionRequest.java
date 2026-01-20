package com.bvas.bvas.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BillSubmissionRequest {
    
    @NotNull(message = "Bill month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer billMonth;
    
    @NotNull(message = "Bill year is required")
    @Min(value = 2020, message = "Year must be valid")
    @Max(value = 2100, message = "Year must be valid")
    private Integer billYear;
    
    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String vendorRemarks;
    
    @NotEmpty(message = "At least one bill item is required")
    private List<BillItemRequest> billItems;
    
    @Data
    public static class BillItemRequest {
        @NotNull(message = "District ID is required")
        private Long districtId;
        
        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Quantity format is invalid")
        private BigDecimal quantity;
        
        @Digits(integer = 15, fraction = 2, message = "Amount format is invalid")
        private BigDecimal amount;
    }
}