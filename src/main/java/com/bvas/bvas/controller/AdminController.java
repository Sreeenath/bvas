package com.bvas.bvas.controller;

import com.bvas.bvas.dto.request.BillSubmissionWindowRequest;
import com.bvas.bvas.dto.request.UserCreateRequest;
import com.bvas.bvas.dto.response.*;
import com.bvas.bvas.model.District;
import com.bvas.bvas.model.User;
import com.bvas.bvas.model.enums.UserRole;
import com.bvas.bvas.repository.DistrictRepository;
import com.bvas.bvas.service.BillSubmissionWindowService;
import com.bvas.bvas.service.DashboardService;
import com.bvas.bvas.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.bvas.bvas.model.AuditLog;
import com.bvas.bvas.repository.AuditLogRepository;
import com.bvas.bvas.service.DtoMapperService;
import com.bvas.bvas.service.DummyEmailService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final DashboardService dashboardService;
    private final BillSubmissionWindowService submissionWindowService;
    private final DistrictRepository districtRepository;
    private final AuditLogRepository auditLogRepository;
    private final DtoMapperService dtoMapper;
    private final DummyEmailService emailService;

    @GetMapping("/dashboard/kpis")
    public ResponseEntity<ApiResponse<DashboardKpiResponse>> getDashboardKPIs() {
        DashboardKpiResponse kpis = dashboardService.getDashboardKPIs();
        return ResponseEntity.ok(ApiResponse.success(kpis));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request,
            Authentication authentication) {
        
        // Check if username exists
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Username already exists"));
        }

        // Check if email exists
        if (request.getEmail() != null && userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email already exists"));
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setRole(UserRole.valueOf(request.getRole()));
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        user.setIsApproved(true); // Admin-created users are auto-approved
        user.setCreatedBy(authentication.getName());

        // Assign districts if role is DISTRICT_VERIFIER
        if (user.getRole() == UserRole.DISTRICT_VERIFIER && request.getDistrictIds() != null) {
            Set<District> districts = request.getDistrictIds().stream()
                .map(districtId -> districtRepository.findById(districtId)
                    .orElseThrow(() -> new RuntimeException("District not found: " + districtId)))
                .collect(Collectors.toSet());
            user.setAssignedDistricts(districts);
        }

        user = userService.save(user);
        // Send email to newly created user
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailService.sendUserCreatedEmail(
                user.getEmail(),
                user.getUsername(),
                request.getPassword(), // Send password only in email
                user.getFullName(),
                user.getRole().name()
            );
        }

        return ResponseEntity.ok(ApiResponse.success("User created successfully", 
            convertToUserResponse(user)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isApproved) {
        
        List<User> users;
        
        if (role != null) {
            if (isActive != null) {
                users = userService.findByRoleAndIsActive(role, isActive);
            } else {
                users = userService.findByRole(role);
            }
        } else {
            users = userService.findAll();
        }

        // Filter by approval status if specified
        if (isApproved != null) {
            users = users.stream()
                .filter(user -> user.getIsApproved().equals(isApproved))
                .collect(Collectors.toList());
        }

        List<UserResponse> userResponses = users.stream()
            .map(this::convertToUserResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(userResponses));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(convertToUserResponse(user)));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserCreateRequest request) {
        
        User user = userService.findById(id);
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        // Update districts if role is DISTRICT_VERIFIER
        if (user.getRole() == UserRole.DISTRICT_VERIFIER && request.getDistrictIds() != null) {
            Set<District> districts = request.getDistrictIds().stream()
                .map(districtId -> districtRepository.findById(districtId)
                    .orElseThrow(() -> new RuntimeException("District not found: " + districtId)))
                .collect(Collectors.toSet());
            user.setAssignedDistricts(districts);
        }

        user = userService.save(user);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", 
            convertToUserResponse(user)));
    }

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<ApiResponse<UserResponse>> approveUser(@PathVariable Long id) {
        User user = userService.findById(id);
        user.setIsApproved(true);
        user = userService.save(user);
        
        // Send approval email
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailService.sendApprovalEmail(
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name()
            );
        }
        
        return ResponseEntity.ok(ApiResponse.success("User approved", convertToUserResponse(user)));
    }

    // reject user endpoint
    @PutMapping("/users/{id}/reject")
    public ResponseEntity<ApiResponse<UserResponse>> rejectUser(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        User user = userService.findById(id);
        
        // Don't delete user, just mark as not approved and send rejection email
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailService.sendRejectionEmail(
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                reason != null ? reason : "Your registration could not be approved at this time."
            );
        }
        
        return ResponseEntity.ok(ApiResponse.success("Rejection email sent", convertToUserResponse(user)));
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<ApiResponse<UserResponse>> blockUser(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {
        User user = userService.findById(id);
        user.setIsActive(false);
        user.setRemarks(remarks);
        user = userService.save(user);
        return ResponseEntity.ok(ApiResponse.success("User blocked", convertToUserResponse(user)));
    }

    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<ApiResponse<UserResponse>> unblockUser(@PathVariable Long id) {
        User user = userService.findById(id);
        user.setIsActive(true);
        user = userService.save(user);
        return ResponseEntity.ok(ApiResponse.success("User unblocked", convertToUserResponse(user)));
    }

    @PostMapping("/submission-windows")
    public ResponseEntity<ApiResponse<?>> manageSubmissionWindow(
            @Valid @RequestBody BillSubmissionWindowRequest request,
            Authentication authentication) {
        
        User admin = userService.findByUsername(authentication.getName());
        
        var window = submissionWindowService.createOrUpdateWindow(
            request.getMonth(),
            request.getYear(),
            request.getIsLocked(),
            request.getLockReason(),
            admin
        );

        String message = request.getIsLocked() 
            ? "Submission window locked for " + request.getMonth() + "/" + request.getYear()
            : "Submission window unlocked for " + request.getMonth() + "/" + request.getYear();

        return ResponseEntity.ok(ApiResponse.success(message, window));
    }

    @GetMapping("/submission-windows")
    public ResponseEntity<ApiResponse<List<?>>> getSubmissionWindows(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        
        List<?> windows;
        if (month != null && year != null) {
            var window = submissionWindowService.getWindow(month, year);
            windows = window != null ? List.of(window) : List.of();
        } else {
            // Get all windows - would need repository method
            windows = List.of();
        }
        
        return ResponseEntity.ok(ApiResponse.success(windows));
    }

    private UserResponse convertToUserResponse(User user) {
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
        
        // Convert districts
        if (user.getAssignedDistricts() != null) {
            response.setAssignedDistricts(user.getAssignedDistricts().stream()
                .map(district -> {
                    var districtResponse = new com.bvas.bvas.dto.response.DistrictResponse();
                    districtResponse.setId(district.getId());
                    districtResponse.setCode(district.getCode());
                    districtResponse.setName(district.getName());
                    districtResponse.setDescription(district.getDescription());
                    districtResponse.setIsActive(district.getIsActive());
                    return districtResponse;
                })
                .collect(Collectors.toList()));
        }
        
        return response;
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long userId) {
        
        List<AuditLog> auditLogs;
        
        if (entityType != null && entityId != null) {
            auditLogs = auditLogRepository.findByEntity(entityType, entityId);
        } else if (userId != null) {
            User user = userService.findById(userId);
            auditLogs = auditLogRepository.findByPerformedBy(user);
        } else {
            auditLogs = auditLogRepository.findAll();
        }
        
        // Filter by action if provided
        if (action != null) {
            auditLogs = auditLogs.stream()
                .filter(log -> log.getAction().name().equalsIgnoreCase(action))
                .collect(Collectors.toList());
        }
        
        List<AuditLogResponse> responses = dtoMapper.toAuditLogResponseList(auditLogs);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/emails")
    public ResponseEntity<ApiResponse<Map<String, List<DummyEmailService.EmailRecord>>>> getAllEmails() {
        Map<String, List<DummyEmailService.EmailRecord>> emails = emailService.getAllEmails();
        return ResponseEntity.ok(ApiResponse.success(emails));
    }

    @GetMapping("/emails/{emailAddress}")
    public ResponseEntity<ApiResponse<List<DummyEmailService.EmailRecord>>> getEmailsForUser(
            @PathVariable String emailAddress) {
        List<DummyEmailService.EmailRecord> emails = emailService.getEmailsFor(emailAddress);
        return ResponseEntity.ok(ApiResponse.success(emails));
    }

    @GetMapping("/districts")
    public ResponseEntity<ApiResponse<List<DistrictResponse>>> getAllDistricts() {
        List<District> districts = districtRepository.findByIsActiveTrue();
        List<DistrictResponse> responses = districts.stream()
            .map(dtoMapper::toDistrictResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}