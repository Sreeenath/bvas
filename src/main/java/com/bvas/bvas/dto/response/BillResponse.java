package com.bvas.bvas.dto.response;

import com.bvas.bvas.model.enums.BillStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BillResponse {
    private Long id;
    private UserResponse vendor;
    private Integer billMonth;
    private Integer billYear;
    private BillStatus status;
    private BigDecimal totalAmount;
    private List<BillItemResponse> billItems;
    private List<BillDocumentResponse> documents;
    private DigitalSignatureResponse digitalSignature;
    private UserResponse verifiedBy;
    private String rejectionRemarks;
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
    private String vendorRemarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}