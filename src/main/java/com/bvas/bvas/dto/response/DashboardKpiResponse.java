package com.bvas.bvas.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class DashboardKpiResponse {
    private Long totalPendingBills;
    private Long totalApprovedBills;
    private Long totalRejectedBills;
    private Long currentMonthPending;
    private Long currentMonthApproved;
    private Long currentMonthRejected;
    private Long previousMonthPending;
    private Long previousMonthApproved;
    private Long previousMonthRejected;
    private Map<String, Long> districtWiseCounts; // District code -> count
}