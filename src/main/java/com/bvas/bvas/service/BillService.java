package com.bvas.bvas.service;

import com.bvas.bvas.dto.request.BillSubmissionRequest;
import com.bvas.bvas.exception.*;
import com.bvas.bvas.model.*;
import com.bvas.bvas.model.enums.BillStatus;
import com.bvas.bvas.model.enums.DocumentType;
import com.bvas.bvas.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final BillDocumentRepository billDocumentRepository;
    private final DistrictRepository districtRepository;
    private final BillSubmissionWindowRepository submissionWindowRepository;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    private final String billNotFoungMsg = "Bill not found with id: ";

    @Transactional
    public Bill createBill(BillSubmissionRequest request, User vendor, List<MultipartFile> files) {
        // Check if submission window is locked
        BillSubmissionWindow window = submissionWindowRepository
            .findByMonthAndYear(request.getBillMonth(), request.getBillYear())
            .orElse(null);
        
        if (window != null && window.getIsLocked()) {
            throw new BillSubmissionLockedException("Bill submission is locked for " + 
                request.getBillMonth() + "/" + request.getBillYear());
        }

        // Check if bill already exists for this month/year
        if (billRepository.existsByVendorAndBillMonthAndBillYear(
                vendor, request.getBillMonth(), request.getBillYear())) {
            throw new BillAlreadyExistsException("Bill already exists for this month/year");
        }

        // Create bill
        Bill bill = new Bill();
        bill.setVendor(vendor);
        bill.setBillMonth(request.getBillMonth());
        bill.setBillYear(request.getBillYear());
        bill.setStatus(BillStatus.PENDING_DISTRICT_VERIFICATION);
        bill.setVendorRemarks(request.getVendorRemarks());
        bill.setSubmittedAt(LocalDateTime.now());
        
        // Calculate total amount
        BigDecimal totalAmount = request.getBillItems().stream()
            .map(BillSubmissionRequest.BillItemRequest::getAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        bill.setTotalAmount(totalAmount);

        Bill savedBill = billRepository.save(bill);

        // Create bill items
        List<BillItem> billItems = request.getBillItems().stream()
                .map(itemRequest -> {
                    BillItem item = new BillItem();
                    item.setBill(savedBill);
                    item.setDistrict(
                            districtRepository.findById(itemRequest.getDistrictId())
                                    .orElseThrow(() -> new ResourceNotFoundException(
                                            "District not found: " + itemRequest.getDistrictId()))
                    );
                    item.setQuantity(itemRequest.getQuantity());
                    item.setAmount(itemRequest.getAmount());
                    return item;
                })
                .collect(Collectors.toList());

        billItemRepository.saveAll(billItems);
        savedBill.setBillItems(billItems);

        // Save uploaded files
        if (files != null && !files.isEmpty()) {
            final Bill finalBill = savedBill;

            List<BillDocument> documents = files.stream()
                    .map(file -> saveDocument(
                            finalBill,
                            file,
                            determineDocumentType(file.getOriginalFilename())))
                    .collect(Collectors.toList());

            billDocumentRepository.saveAll(documents);
        }

        // Audit log
        auditLogService.logBillSubmission(savedBill, vendor);
        return savedBill;
    }

    @Transactional
    public Bill approveBill(Long billId, User verifier, String remarks) {
        Bill bill = billRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException(billNotFoungMsg + billId));

        if (bill.getStatus() != BillStatus.PENDING_DISTRICT_VERIFICATION) {
            throw new InvalidBillStatusException("Bill is not in pending status. Current status: " + bill.getStatus());
        }

        bill.setStatus(BillStatus.APPROVED);
        bill.setVerifiedBy(verifier);
        bill.setVerifiedAt(LocalDateTime.now());
        bill = billRepository.save(bill);

        auditLogService.logBillApproval(bill, verifier, remarks);
        return bill;
    }

    @Transactional
    public Bill rejectBill(Long billId, User verifier, String remarks) {
        if (remarks == null || remarks.trim().isEmpty()) {
            throw new BadRequestException("Rejection remarks are mandatory");
        }

        Bill bill = billRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException(billNotFoungMsg + billId));

        if (bill.getStatus() != BillStatus.PENDING_DISTRICT_VERIFICATION) {
            throw new InvalidBillStatusException("Bill is not in pending status. Current status: " + bill.getStatus());
        }

        bill.setStatus(BillStatus.REJECTED);
        bill.setVerifiedBy(verifier);
        bill.setRejectionRemarks(remarks);
        bill.setVerifiedAt(LocalDateTime.now());
        bill = billRepository.save(bill);

        auditLogService.logBillRejection(bill, verifier, remarks);
        return bill;
    }

    @Transactional
    public Bill resubmitBill(Long billId, BillSubmissionRequest request, User vendor, List<MultipartFile> files) {
        Bill existingBill = billRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException(billNotFoungMsg + billId));

        if (!existingBill.getVendor().getId().equals(vendor.getId())) {
            throw new ForbiddenException("You can only resubmit your own bills");
        }

        if (existingBill.getStatus() != BillStatus.REJECTED) {
            throw new InvalidBillStatusException("Only rejected bills can be resubmitted. Current status: " + existingBill.getStatus());
        }

        // Update bill with new data
        existingBill.setBillMonth(request.getBillMonth());
        existingBill.setBillYear(request.getBillYear());
        existingBill.setStatus(BillStatus.PENDING_DISTRICT_VERIFICATION);
        existingBill.setVendorRemarks(request.getVendorRemarks());
        existingBill.setRejectionRemarks(null);
        existingBill.setSubmittedAt(LocalDateTime.now());
        existingBill.setVerifiedBy(null);
        existingBill.setVerifiedAt(null);

        // Calculate total amount
        BigDecimal totalAmount = request.getBillItems().stream()
            .map(BillSubmissionRequest.BillItemRequest::getAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        existingBill.setTotalAmount(totalAmount);

        // Delete old bill items
        billItemRepository.deleteByBill(existingBill);
        
        // Create new bill items
        List<BillItem> billItems = request.getBillItems().stream()
            .map(itemRequest -> {
                BillItem item = new BillItem();
                item.setBill(existingBill);
                item.setDistrict(districtRepository.findById(itemRequest.getDistrictId())
                    .orElseThrow(() -> new ResourceNotFoundException("District not found: " + itemRequest.getDistrictId())));
                item.setQuantity(itemRequest.getQuantity());
                item.setAmount(itemRequest.getAmount());
                return item;
            })
            .collect(Collectors.toList());
        billItemRepository.saveAll(billItems);
        existingBill.setBillItems(billItems);

        // Delete old documents
        billDocumentRepository.deleteByBill(existingBill);

        // Save new uploaded files
        if (files != null && !files.isEmpty()) {
            List<BillDocument> documents = files.stream()
                .map(file -> saveDocument(existingBill, file, determineDocumentType(file.getOriginalFilename())))
                .collect(Collectors.toList());
            billDocumentRepository.saveAll(documents);
        }

        // Audit log
        auditLogService.logBillResubmission(existingBill, vendor);

        return existingBill;
    }

    public List<Bill> getPendingBillsByDistrict(Long districtId) {
        return billRepository.findPendingBillsByDistrict(districtId, BillStatus.PENDING_DISTRICT_VERIFICATION);
    }

    public List<Bill> getBillsByVendor(User vendor) {
        return billRepository.findByVendor(vendor);
    }

    public List<Bill> getBillsByVendorAndStatus(User vendor, BillStatus status) {
        return billRepository.findByVendorAndStatus(vendor, status);
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(billNotFoungMsg + id));
    }

    private BillDocument saveDocument(Bill bill, MultipartFile file, DocumentType documentType) {
        String filePath = fileStorageService.storeFile(file);
        BillDocument document = new BillDocument();
        document.setBill(bill);
        document.setDocumentType(documentType);
        document.setFileName(file.getOriginalFilename());
        document.setFilePath(filePath);
        document.setFileSize(file.getSize());
        document.setMimeType(file.getContentType());
        document.setFileHash(fileStorageService.calculateHash(file));
        return document;
    }

    private DocumentType determineDocumentType(String filename) {
        if (filename == null) return DocumentType.SUPPORTING_DOCUMENT;
        String lower = filename.toLowerCase();
        if (lower.contains("epos") || lower.contains("pos")) {
            return DocumentType.EPOS_REPORT;
        }
        if (lower.contains("bill") || lower.contains("main")) {
            return DocumentType.MAIN_BILL;
        }
        return DocumentType.SUPPORTING_DOCUMENT;
    }

    private final EposIntegrationService eposIntegrationService;

    public Bill createBill(Bill bill) {

        if (bill.getBillItems() != null) {
            Map<Long, BigDecimal> eposQuantities =
                    eposIntegrationService.fetchEposQuantities(
                            bill.getBillMonth(),
                            bill.getBillYear(),
                            null
                    );

            bill.getBillItems().forEach(item -> {
                BigDecimal eposQty =
                        eposQuantities.get(item.getDistrict().getId());

                if (eposQty != null) {
                    item.setEposQuantity(eposQty);
                }
            });
        }

        return bill;
    }
}