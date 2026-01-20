package com.bvas.bvas.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BillSubmissionWindowResponse {
    private Long id;
    private Integer month;
    private Integer year;
    private Boolean isLocked;
    private String lockReason;
    private UserResponse lockedBy;
    private LocalDateTime lockedAt;
    private LocalDateTime unlockedAt;
    private String notes;
    private LocalDateTime createdAt;
}