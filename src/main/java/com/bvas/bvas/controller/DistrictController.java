package com.bvas.bvas.controller;

import com.bvas.bvas.dto.request.BillApprovalRequest;
import com.bvas.bvas.dto.response.ApiResponse;
import com.bvas.bvas.dto.response.BillResponse;
import com.bvas.bvas.model.Bill;
import com.bvas.bvas.model.User;
import com.bvas.bvas.service.BillService;
import com.bvas.bvas.service.DtoMapperService;
import com.bvas.bvas.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/district")
@RequiredArgsConstructor
public class DistrictController {

    private final BillService billService;
    private final UserService userService;
    private final DtoMapperService dtoMapper;


    @GetMapping("/bills/pending")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getPendingBills(Authentication authentication) {
        User verifier = userService.findByUsername(authentication.getName());
        
        // Get first assigned district (simplified - would need proper logic)
        Long districtId = verifier.getAssignedDistricts().stream()
            .findFirst()
            .map(d -> d.getId())
            .orElseThrow(() -> new RuntimeException("No district assigned"));
        
        List<Bill> bills = billService.getPendingBillsByDistrict(districtId);
        
        return ResponseEntity.ok(ApiResponse.success(
            bills.stream().map(this::convertToResponse).collect(Collectors.toList())));
    }

    @GetMapping("/bills/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> getBill(@PathVariable Long id) {
        Bill bill = billService.getBillById(id);
        return ResponseEntity.ok(ApiResponse.success(convertToResponse(bill)));
    }

    @PostMapping("/bills/approve")
    public ResponseEntity<ApiResponse<BillResponse>> approveBill(
            @Valid @RequestBody BillApprovalRequest request,
            Authentication authentication) {
        
        User verifier = userService.findByUsername(authentication.getName());
        
        // Re-authentication check would go here
        
        if ("APPROVE".equalsIgnoreCase(request.getAction())) {
            Bill bill = billService.approveBill(request.getBillId(), verifier, request.getRemarks());
            return ResponseEntity.ok(ApiResponse.success("Bill approved", convertToResponse(bill)));
        } else if ("REJECT".equalsIgnoreCase(request.getAction())) {
            Bill bill = billService.rejectBill(request.getBillId(), verifier, request.getRemarks());
            return ResponseEntity.ok(ApiResponse.success("Bill rejected", convertToResponse(bill)));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid action"));
        }
    }

    private BillResponse convertToResponse(Bill bill) {
        return dtoMapper.toBillResponse(bill);
    }
}