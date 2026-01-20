package com.bvas.bvas.controller;

import com.bvas.bvas.dto.request.BillSubmissionRequest;
import com.bvas.bvas.dto.response.ApiResponse;
import com.bvas.bvas.dto.response.BillResponse;
import com.bvas.bvas.exception.BadRequestException;
import com.bvas.bvas.exception.ResourceNotFoundException;
import com.bvas.bvas.model.Bill;
import com.bvas.bvas.model.User;
import com.bvas.bvas.repository.BillDocumentRepository;
import com.bvas.bvas.security.JwtTokenUtil;
import com.bvas.bvas.service.BillService;
import com.bvas.bvas.service.DtoMapperService;
import com.bvas.bvas.service.FileStorageService;
import com.bvas.bvas.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.bvas.bvas.exception.ForbiddenException;
import com.bvas.bvas.model.BillDocument;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final BillService billService;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final DtoMapperService dtoMapper;
    private final BillDocumentRepository billDocumentRepository;
    private final FileStorageService fileStorageService;

    @PostMapping("/bills")
    @Transactional
    public ResponseEntity<ApiResponse<BillResponse>> submitBill(
            @Valid @ModelAttribute BillSubmissionRequest request,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) {
        
        User vendor = userService.findByUsername(authentication.getName());
        Bill bill = billService.createBill(request, vendor, files);
        
       return ResponseEntity.ok(ApiResponse.success("Bill submitted successfully",
        dtoMapper.toBillResponse(bill)));
    }

    @GetMapping("/bills")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getMyBills(Authentication authentication) {
        User vendor = userService.findByUsername(authentication.getName());
        List<Bill> bills = billService.getBillsByVendor(vendor);
        
        return ResponseEntity.ok(ApiResponse.success(
            bills.stream().map(this::convertToResponse).collect(Collectors.toList())));
    }

    @GetMapping("/bills/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> getBill(@PathVariable Long id, Authentication authentication) {
        User vendor = userService.findByUsername(authentication.getName());
        Bill bill = billService.getBillById(id);
        
        if (!bill.getVendor().getId().equals(vendor.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(bill)));
    }


    private BillResponse convertToResponse(Bill bill) {
        return dtoMapper.toBillResponse(bill);
    }

    @GetMapping("/bills/{billId}/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long billId,
            @PathVariable Long documentId,
            Authentication authentication) {
        
        User vendor = userService.findByUsername(authentication.getName());
        Bill bill = billService.getBillById(billId);
        
        if (!bill.getVendor().getId().equals(vendor.getId())) {
            throw new ForbiddenException("Access denied");
        }
        
        BillDocument document = billDocumentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        
        if (!document.getBill().getId().equals(billId)) {
            throw new BadRequestException("Document does not belong to this bill");
        }
        
        byte[] fileContent = fileStorageService.loadFile(document.getFilePath());
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
            .contentType(MediaType.parseMediaType(document.getMimeType() != null ? document.getMimeType() : "application/octet-stream"))
            .body(resource);
    }

    @PostMapping("/bills/{id}/resubmit")
    @Transactional
    public ResponseEntity<ApiResponse<BillResponse>> resubmitBill(
            @PathVariable Long id,
            @Valid @ModelAttribute BillSubmissionRequest request,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) {
        
        User vendor = userService.findByUsername(authentication.getName());
        Bill bill = billService.resubmitBill(id, request, vendor, files);
        
       return ResponseEntity.ok(ApiResponse.success("Bill submitted successfully",
        dtoMapper.toBillResponse(bill)));
    }
}