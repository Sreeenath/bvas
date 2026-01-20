package com.bvas.bvas.service;

import com.bvas.bvas.dto.response.DashboardKpiResponse;
import com.bvas.bvas.model.enums.BillStatus;
import com.bvas.bvas.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BillRepository billRepository;

    public DashboardKpiResponse getDashboardKPIs() {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        int previousMonth = currentMonth == 1 ? 12 : currentMonth - 1;
        int previousYear = currentMonth == 1 ? currentYear - 1 : currentYear;

        DashboardKpiResponse kpis = new DashboardKpiResponse();

        // Overall counts
        kpis.setTotalPendingBills(
                (long) billRepository.findByMonthAndYearAndStatus(null, null, BillStatus.PENDING_DISTRICT_VERIFICATION).size());
        kpis.setTotalApprovedBills(
                (long) billRepository.findByMonthAndYearAndStatus(null, null, BillStatus.APPROVED).size());
        kpis.setTotalRejectedBills(
                (long) billRepository.findByMonthAndYearAndStatus(null, null, BillStatus.REJECTED).size());

        // Current month
        kpis.setCurrentMonthPending(
                (long) billRepository.findByMonthAndYearAndStatus(currentMonth, currentYear, BillStatus.PENDING_DISTRICT_VERIFICATION).size());
        kpis.setCurrentMonthApproved(
                (long) billRepository.findByMonthAndYearAndStatus(currentMonth, currentYear, BillStatus.APPROVED).size());
        kpis.setCurrentMonthRejected(
                (long) billRepository.findByMonthAndYearAndStatus(currentMonth, currentYear, BillStatus.REJECTED).size());

        // Previous month
        kpis.setPreviousMonthPending(
                (long) billRepository.findByMonthAndYearAndStatus(previousMonth, previousYear, BillStatus.PENDING_DISTRICT_VERIFICATION).size());
        kpis.setPreviousMonthApproved(
                (long) billRepository.findByMonthAndYearAndStatus(previousMonth, previousYear, BillStatus.APPROVED).size());
        kpis.setPreviousMonthRejected(
                (long) billRepository.findByMonthAndYearAndStatus(previousMonth, previousYear, BillStatus.REJECTED).size());

        // District-wise counts (simplified - would need proper aggregation)
        kpis.setDistrictWiseCounts(new HashMap<>());

        return kpis;
    }
}