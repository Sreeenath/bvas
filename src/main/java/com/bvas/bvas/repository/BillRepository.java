package com.bvas.bvas.repository;

import com.bvas.bvas.model.Bill;
import com.bvas.bvas.model.User;
import com.bvas.bvas.model.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    List<Bill> findByVendor(User vendor);
    
    List<Bill> findByVendorAndStatus(User vendor, BillStatus status);
    
    List<Bill> findByVendorAndBillMonthAndBillYear(User vendor, Integer month, Integer year);
    
    @Query("SELECT b FROM Bill b WHERE b.status = :status AND EXISTS " +
           "(SELECT d FROM b.vendor.assignedDistricts d WHERE d.id = :districtId)")
    List<Bill> findByStatusAndDistrict(@Param("status") BillStatus status, @Param("districtId") Long districtId);
    
    @Query("SELECT b FROM Bill b JOIN b.vendor.assignedDistricts d WHERE d.id = :districtId AND b.status = :status")
    List<Bill> findPendingBillsByDistrict(@Param("districtId") Long districtId, @Param("status") BillStatus status);
    
    @Query("SELECT b FROM Bill b WHERE b.billMonth = :month AND b.billYear = :year")
    List<Bill> findByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);
    
    @Query("SELECT b FROM Bill b WHERE b.billMonth = :month AND b.billYear = :year AND b.status = :status")
    List<Bill> findByMonthAndYearAndStatus(@Param("month") Integer month, @Param("year") Integer year, @Param("status") BillStatus status);
    
    Optional<Bill> findByIdAndVendor(Long id, User vendor);
    
    boolean existsByVendorAndBillMonthAndBillYear(User vendor, Integer month, Integer year);
}