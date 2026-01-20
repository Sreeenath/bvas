package com.bvas.bvas.repository;

import com.bvas.bvas.model.BillDocument;
import com.bvas.bvas.model.Bill;
import com.bvas.bvas.model.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillDocumentRepository extends JpaRepository<BillDocument, Long> {
    
    List<BillDocument> findByBill(Bill bill);
    
    List<BillDocument> findByBillAndDocumentType(Bill bill, DocumentType documentType);
    
    void deleteByBill(Bill bill);
}