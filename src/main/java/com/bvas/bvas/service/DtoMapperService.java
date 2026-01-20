package com.bvas.bvas.service;

import com.bvas.bvas.dto.response.*;
import com.bvas.bvas.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DtoMapperService {

    public BillResponse toBillResponse(Bill bill) {
        if (bill == null) return null;

        BillResponse response = new BillResponse();
        response.setId(bill.getId());
        response.setVendor(toUserResponse(bill.getVendor()));
        response.setBillMonth(bill.getBillMonth());
        response.setBillYear(bill.getBillYear());
        response.setStatus(bill.getStatus());
        response.setTotalAmount(bill.getTotalAmount());
        response.setVendorRemarks(bill.getVendorRemarks());
        response.setRejectionRemarks(bill.getRejectionRemarks());
        response.setSubmittedAt(bill.getSubmittedAt());
        response.setVerifiedAt(bill.getVerifiedAt());
        response.setCreatedAt(bill.getCreatedAt());
        response.setUpdatedAt(bill.getUpdatedAt());

        if (bill.getVerifiedBy() != null) {
            response.setVerifiedBy(toUserResponse(bill.getVerifiedBy()));
        }

        if (bill.getBillItems() != null) {
            response.setBillItems(bill.getBillItems().stream()
                .map(this::toBillItemResponse)
                .collect(Collectors.toList()));
        }

        if (bill.getDocuments() != null) {
            response.setDocuments(bill.getDocuments().stream()
                .map(this::toBillDocumentResponse)
                .collect(Collectors.toList()));
        }

        if (bill.getDigitalSignature() != null) {
            response.setDigitalSignature(toDigitalSignatureResponse(bill.getDigitalSignature()));
        }

        return response;
    }

    public BillItemResponse toBillItemResponse(BillItem billItem) {
        if (billItem == null) return null;

        BillItemResponse response = new BillItemResponse();
        response.setId(billItem.getId());
        response.setDistrict(toDistrictResponse(billItem.getDistrict()));
        response.setQuantity(billItem.getQuantity());
        response.setEposQuantity(billItem.getEposQuantity());
        response.setHasDiscrepancy(billItem.getHasDiscrepancy());
        response.setDiscrepancyNotes(billItem.getDiscrepancyNotes());
        response.setAmount(billItem.getAmount());
        return response;
    }

    public BillDocumentResponse toBillDocumentResponse(BillDocument document) {
        if (document == null) return null;

        BillDocumentResponse response = new BillDocumentResponse();
        response.setId(document.getId());
        response.setDocumentType(document.getDocumentType());
        response.setFileName(document.getFileName());
        response.setFilePath(document.getFilePath());
        response.setFileSize(document.getFileSize());
        response.setMimeType(document.getMimeType());
        response.setDescription(document.getDescription());
        return response;
    }

    public DigitalSignatureResponse toDigitalSignatureResponse(DigitalSignature signature) {
        if (signature == null) return null;

        DigitalSignatureResponse response = new DigitalSignatureResponse();
        response.setId(signature.getId());
        response.setSignatureType(signature.getSignatureType());
        response.setSignedDocumentPath(signature.getSignedDocumentPath());
        response.setDocumentHash(signature.getDocumentHash());
        response.setSignedDocumentHash(signature.getSignedDocumentHash());
        response.setSignerName(signature.getSignerName());
        response.setCertificateSerialNumber(signature.getCertificateSerialNumber());
        response.setCertificateIssuer(signature.getCertificateIssuer());
        response.setCertificateValidFrom(signature.getCertificateValidFrom());
        response.setCertificateValidTo(signature.getCertificateValidTo());
        response.setSignedAt(signature.getSignedAt());
        response.setSignerIpAddress(signature.getSignerIpAddress());
        response.setTimestampAuthorityUrl(signature.getTimestampAuthorityUrl());
        response.setIsPadesCompliant(signature.getIsPadesCompliant());
        response.setIsLongTermValidation(signature.getIsLongTermValidation());
        response.setIsValid(signature.getIsValid());
        return response;
    }

    public UserResponse toUserResponse(User user) {
        if (user == null) return null;

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setMobileNumber(user.getMobileNumber());
        response.setRole(user.getRole());
        response.setIsActive(user.getIsActive());
        response.setIsApproved(user.getIsApproved());
        response.setTwoFactorEnabled(user.getTwoFactorEnabled());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());

        if (user.getAssignedDistricts() != null) {
            response.setAssignedDistricts(user.getAssignedDistricts().stream()
                .map(this::toDistrictResponse)
                .collect(Collectors.toList()));
        }

        return response;
    }

    public DistrictResponse toDistrictResponse(District district) {
        if (district == null) return null;

        DistrictResponse response = new DistrictResponse();
        response.setId(district.getId());
        response.setCode(district.getCode());
        response.setName(district.getName());
        response.setDescription(district.getDescription());
        response.setIsActive(district.getIsActive());
        return response;
    }

    public AuditLogResponse toAuditLogResponse(AuditLog auditLog) {
        if (auditLog == null) return null;

        AuditLogResponse response = new AuditLogResponse();
        response.setId(auditLog.getId());
        response.setAction(auditLog.getAction());
        response.setEntityType(auditLog.getEntityType());
        response.setEntityId(auditLog.getEntityId());
        response.setIpAddress(auditLog.getIpAddress());
        response.setDetails(auditLog.getDetails());
        response.setRemarks(auditLog.getRemarks());
        response.setActionTimestamp(auditLog.getActionTimestamp());

        if (auditLog.getPerformedBy() != null) {
            response.setPerformedBy(toUserResponse(auditLog.getPerformedBy()));
        }

        return response;
    }

    public BillSubmissionWindowResponse toBillSubmissionWindowResponse(BillSubmissionWindow window) {
        if (window == null) return null;

        BillSubmissionWindowResponse response = new BillSubmissionWindowResponse();
        response.setId(window.getId());
        response.setMonth(window.getMonth());
        response.setYear(window.getYear());
        response.setIsLocked(window.getIsLocked());
        response.setLockReason(window.getLockReason());
        response.setLockedAt(window.getLockedAt());
        response.setUnlockedAt(window.getUnlockedAt());
        response.setNotes(window.getNotes());
        response.setCreatedAt(window.getCreatedAt());

        if (window.getLockedBy() != null) {
            response.setLockedBy(toUserResponse(window.getLockedBy()));
        }

        return response;
    }

    public List<BillResponse> toBillResponseList(List<Bill> bills) {
        if (bills == null) return null;
        return bills.stream()
            .map(this::toBillResponse)
            .collect(Collectors.toList());
    }

    public List<UserResponse> toUserResponseList(List<User> users) {
        if (users == null) return null;
        return users.stream()
            .map(this::toUserResponse)
            .collect(Collectors.toList());
    }

    public List<AuditLogResponse> toAuditLogResponseList(List<AuditLog> auditLogs) {
        if (auditLogs == null) return null;
        return auditLogs.stream()
            .map(this::toAuditLogResponse)
            .collect(Collectors.toList());
    }
}