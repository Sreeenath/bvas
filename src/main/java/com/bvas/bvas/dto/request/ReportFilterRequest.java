package com.bvas.bvas.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ReportFilterRequest {
    
    private Integer startMonth;
    private Integer startYear;
    private Integer endMonth;
    private Integer endYear;
    private List<Long> districtIds;
    private List<Long> vendorIds;
    private List<String> statuses; // "PENDING_DISTRICT_VERIFICATION", "APPROVED", "REJECTED"
    private String reportFormat; // "EXCEL" or "PDF"
    private Boolean includeSignedPdf = false; // For digitally signed PDF reports
}