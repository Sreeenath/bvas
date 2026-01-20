package com.bvas.bvas.controller;

import com.bvas.bvas.dto.request.DigitalSignatureRequest;
import com.bvas.bvas.dto.response.ApiResponse;
import com.bvas.bvas.dto.response.DashboardKpiResponse;
import com.bvas.bvas.dto.response.DigitalSignatureResponse;
import com.bvas.bvas.model.DigitalSignature;
import com.bvas.bvas.model.User;
import com.bvas.bvas.service.DashboardService;
import com.bvas.bvas.service.DigitalSignatureService;
import com.bvas.bvas.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/district")
@RequiredArgsConstructor
public class DistrictSignatureController {

    private final DigitalSignatureService signatureService;
    private final UserService userService;

    @PostMapping("/bills/sign")
    public ResponseEntity<ApiResponse<DigitalSignatureResponse>> signBill(
            @Valid @RequestBody DigitalSignatureRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        User signer = userService.findByUsername(authentication.getName());
        String ipAddress = httpRequest.getRemoteAddr();
        
        DigitalSignature signature = signatureService.signBill(request, signer, ipAddress);
        
        return ResponseEntity.ok(ApiResponse.success("Bill signed successfully", 
            convertToResponse(signature)));
    }

    private DigitalSignatureResponse convertToResponse(DigitalSignature signature) {
        DigitalSignatureResponse response = new DigitalSignatureResponse();
        response.setId(signature.getId());
        response.setSignatureType(signature.getSignatureType());
        response.setSignedDocumentPath(signature.getSignedDocumentPath());
        response.setSignerName(signature.getSignerName());
        response.setSignedAt(signature.getSignedAt());
        return response;
    }
}