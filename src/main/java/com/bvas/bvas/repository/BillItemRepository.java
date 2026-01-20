package com.bvas.bvas.repository;

import com.bvas.bvas.model.BillItem;
import com.bvas.bvas.model.Bill;
import com.bvas.bvas.model.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    
    List<BillItem> findByBill(Bill bill);
    
    Optional<BillItem> findByBillAndDistrict(Bill bill, District district);
    
    void deleteByBill(Bill bill);
}