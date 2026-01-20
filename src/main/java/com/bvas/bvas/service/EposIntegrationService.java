package com.bvas.bvas.service;

import com.bvas.bvas.model.District;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * DUMMY ePOS Integration Service for Local Development
 * In production, this would integrate with epos.uk.gov.in API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EposIntegrationService {

    @Value("${epos.api.url:https://epos.uk.gov.in/api}")
    private String eposApiUrl;

    @Value("${epos.dummy.enabled:true}")
    private Boolean dummyModeEnabled;

    /**
     * Fetch distribution quantities from ePOS system for a given month/year and district
     */
    public Map<Long, BigDecimal> fetchEposQuantities(Integer month, Integer year, Long districtId) {
        log.warn("DUMMY MODE: Fetching ePOS quantities for district: {}, month: {}/{}", districtId, month, year);
        
        if (dummyModeEnabled) {
            // Return dummy data
            Map<Long, BigDecimal> dummyData = new HashMap<>();
            // Generate some dummy quantities
            for (int i = 1; i <= 10; i++) {
                dummyData.put((long) i, BigDecimal.valueOf(1000 + (i * 100)));
            }
            log.info("DUMMY MODE: Returning dummy ePOS data");
            return dummyData;
        }
        
        // In production, make actual API call
        // return callEposApi(month, year, districtId);
        return new HashMap<>();
    }

    /**
     * Fetch ePOS quantities for all districts
     */
    public Map<Long, Map<Long, BigDecimal>> fetchAllDistrictsEposQuantities(Integer month, Integer year) {
        log.warn("DUMMY MODE: Fetching ePOS quantities for all districts, month: {}/{}", month, year);
        
        Map<Long, Map<Long, BigDecimal>> allDistrictsData = new HashMap<>();
        
        // In dummy mode, return empty map or dummy data
        if (dummyModeEnabled) {
            log.info("DUMMY MODE: Returning empty ePOS data for all districts");
        }
        
        return allDistrictsData;
    }

    /**
     * Compare vendor quantities with ePOS quantities and highlight discrepancies
     */
    public void compareAndHighlightDiscrepancies(Map<Long, BigDecimal> vendorQuantities, 
                                                   Map<Long, BigDecimal> eposQuantities) {
        log.info("Comparing vendor quantities with ePOS quantities");
        
        // In production, this would perform actual comparison
        // For now, just log the comparison
        vendorQuantities.forEach((districtId, vendorQty) -> {
            BigDecimal eposQty = eposQuantities.get(districtId);
            if (eposQty != null) {
                BigDecimal difference = vendorQty.subtract(eposQty);
                if (difference.abs().compareTo(BigDecimal.ZERO) > 0) {
                    log.warn("Discrepancy found for district {}: Vendor={}, ePOS={}, Difference={}", 
                        districtId, vendorQty, eposQty, difference);
                }
            }
        });
    }
}