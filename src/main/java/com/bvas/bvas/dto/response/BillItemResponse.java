package com.bvas.bvas.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillItemResponse {
    private Long id;
    private DistrictResponse district;
    private BigDecimal quantity;
    private BigDecimal eposQuantity;
    private Boolean hasDiscrepancy;
    private String discrepancyNotes;
    private BigDecimal amount;
}