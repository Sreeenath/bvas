package com.bvas.bvas.service;

import com.bvas.bvas.model.AuditLog;
import com.bvas.bvas.model.Bill;
import com.bvas.bvas.model.User;
import com.bvas.bvas.model.enums.AuditAction;
import com.bvas.bvas.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logBillSubmission(Bill bill, User user) {
        AuditLog log = new AuditLog();
        log.setAction(AuditAction.BILL_SUBMITTED);
        log.setPerformedBy(user);
        log.setEntityType("Bill");
        log.setEntityId(bill.getId());
        log.setActionTimestamp(LocalDateTime.now());
        log.setDetails(String.format("Bill submitted for %d/%d", bill.getBillMonth(), bill.getBillYear()));
        auditLogRepository.save(log);
    }

    @Transactional
    public void logBillApproval(Bill bill, User verifier, String remarks) {
        AuditLog log = new AuditLog();
        log.setAction(AuditAction.BILL_APPROVED);
        log.setPerformedBy(verifier);
        log.setEntityType("Bill");
        log.setEntityId(bill.getId());
        log.setActionTimestamp(LocalDateTime.now());
        log.setRemarks(remarks);
        log.setDetails(String.format("Bill approved for %d/%d", bill.getBillMonth(), bill.getBillYear()));
        auditLogRepository.save(log);
    }

    @Transactional
    public void logBillRejection(Bill bill, User verifier, String remarks) {
        AuditLog log = new AuditLog();
        log.setAction(AuditAction.BILL_REJECTED);
        log.setPerformedBy(verifier);
        log.setEntityType("Bill");
        log.setEntityId(bill.getId());
        log.setActionTimestamp(LocalDateTime.now());
        log.setRemarks(remarks);
        log.setDetails(String.format("Bill rejected for %d/%d", bill.getBillMonth(), bill.getBillYear()));
        auditLogRepository.save(log);
    }

    @Transactional
    public void logDigitalSignature(Bill bill, User signer, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setAction(AuditAction.DIGITAL_SIGNATURE_APPLIED);
        log.setPerformedBy(signer);
        log.setEntityType("Bill");
        log.setEntityId(bill.getId());
        log.setIpAddress(ipAddress);
        log.setActionTimestamp(LocalDateTime.now());
        log.setDetails("Digital signature applied to bill");
        auditLogRepository.save(log);
    }

    @Transactional
    public void logBillResubmission(Bill bill, User user) {
        AuditLog log = new AuditLog();
        log.setAction(AuditAction.BILL_RESUBMITTED);
        log.setPerformedBy(user);
        log.setEntityType("Bill");
        log.setEntityId(bill.getId());
        log.setActionTimestamp(LocalDateTime.now());
        log.setDetails(String.format("Bill resubmitted for %d/%d", bill.getBillMonth(), bill.getBillYear()));
        auditLogRepository.save(log);
    }
}