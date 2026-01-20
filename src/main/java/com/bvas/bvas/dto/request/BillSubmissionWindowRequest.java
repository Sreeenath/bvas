package com.bvas.bvas.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillSubmissionWindowRequest {
    
    @NotNull(message = "Month is required")
    @Min(1)
    @Max(12)
    private Integer month;
    
    @NotNull(message = "Year is required")
    private Integer year;
    
    @NotNull(message = "Lock status is required")
    private Boolean isLocked;
    
    private String lockReason;
}