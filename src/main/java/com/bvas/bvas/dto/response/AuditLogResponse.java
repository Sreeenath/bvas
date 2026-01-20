package com.bvas.bvas.dto.response;

import com.bvas.bvas.model.enums.AuditAction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogResponse {
    private Long id;
    private AuditAction action;
    private UserResponse performedBy;
    private String entityType;
    private Long entityId;
    private String ipAddress;
    private String details;
    private String remarks;
    private LocalDateTime actionTimestamp;
}