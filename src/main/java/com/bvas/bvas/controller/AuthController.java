package com.bvas.bvas.controller;

import com.bvas.bvas.dto.request.ForgotPasswordRequest;
import com.bvas.bvas.dto.request.LoginRequest;
import com.bvas.bvas.dto.request.UserRegistrationRequest;
import com.bvas.bvas.dto.request.VerifyOtpAndResetPasswordRequest;
import com.bvas.bvas.dto.response.ApiResponse;
import com.bvas.bvas.exception.ResourceNotFoundException;
import com.bvas.bvas.model.User;
import com.bvas.bvas.model.enums.UserRole;
import com.bvas.bvas.repository.UserRepository;
import com.bvas.bvas.security.JwtTokenUtil;
import com.bvas.bvas.service.DummyEmailService;
import com.bvas.bvas.service.DummyOtpService;
import com.bvas.bvas.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final DummyEmailService emailService;
    private final DummyOtpService otpService;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody UserRegistrationRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Username already exists"));
        }

        if (request.getEmail() != null && userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email already exists"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setRole(UserRole.VENDOR);
        user.setIsActive(true);
        user.setIsApproved(false); // Requires HQ approval

        userService.save(user);

        // Send registration email
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailService.sendRegistrationEmail(
                user.getEmail(),
                user.getUsername(),
                user.getFullName()
            );
        }

        return ResponseEntity.ok(ApiResponse.success("Registration successful. Awaiting approval.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
            User user = userService.findByUsername(request.getUsername());

            // Check if user is approved (for vendors)
            if (user.getRole() == UserRole.VENDOR && !user.getIsApproved()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Account pending approval. Please wait for HQ approval."));
            }

            // Check if user is active
            if (!user.getIsActive()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Account is inactive. Please contact administrator."));
            }

            user.setLastLoginAt(LocalDateTime.now());
            userService.save(user);

            String token = jwtTokenUtil.generateToken(userDetails);

            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setUsername(user.getUsername());
            authResponse.setRole(user.getRole().name());
            authResponse.setFullName(user.getFullName());
            authResponse.setUserId(user.getId());

            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            log.info("Forgot password request received for: {}", request.getUsernameOrMobile());

            // Try by username, else fall back to mobile number
            User user;
            try {
                user = userService.findByUsername(request.getUsernameOrMobile());
            } catch (ResourceNotFoundException e) {
                user = userRepository.findByMobileNumber(request.getUsernameOrMobile())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username or mobile: " + request.getUsernameOrMobile()
                    ));
            }

            if (!user.getIsActive()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Account is inactive. Please contact administrator."));
            }

            if (user.getMobileNumber() == null || user.getMobileNumber().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mobile number not registered. Please contact administrator."));
            }

            String otp = otpService.generateOtp(user.getMobileNumber());

            log.info("DUMMY MODE: Password reset OTP '{}' generated for user: {} (Mobile: {})",
                otp, user.getUsername(), maskMobile(user.getMobileNumber()));

            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                try {
                    emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), otp);
                } catch (Exception e) {
                    // don't fail if dummy email fails
                    log.error("Failed to send password reset email: {}", e.getMessage(), e);
                }
            }

            return ResponseEntity.ok(ApiResponse.success(
                "OTP has been sent to your registered mobile number. Use OTP: " + otp + " (DUMMY MODE)",
                null
            ));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("User not found with the provided username or mobile number."));
        } catch (Exception e) {
            log.error("Failed to process forgot password request: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to process forgot password request: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody VerifyOtpAndResetPasswordRequest request) {
        try {
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("New password and confirm password do not match"));
            }

            // Try by username, else fall back to mobile number
            User user;
            try {
                user = userService.findByUsername(request.getUsernameOrMobile());
            } catch (ResourceNotFoundException e) {
                user = userRepository.findByMobileNumber(request.getUsernameOrMobile())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username or mobile: " + request.getUsernameOrMobile()
                    ));
            }

            if (!user.getIsActive()) {
                return ResponseEntity.status(403)
                    .body(ApiResponse.error("Account is inactive. Please contact administrator."));
            }

            boolean isValidOtp = otpService.validateOtp(user.getMobileNumber(), request.getOtp());
            if (!isValidOtp) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired OTP. Please request a new OTP."));
            }

            user.setPassword(request.getNewPassword());
            userService.save(user);

            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                try {
                    emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getUsername());
                } catch (Exception e) {
                    log.error("Failed to send password reset confirmation email: {}", e.getMessage(), e);
                }
            }

            return ResponseEntity.ok(ApiResponse.success(
                "Password reset successfully. You can now login with your new password.",
                null
            ));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("User not found"));
        } catch (Exception e) {
            log.error("Failed to reset password: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Failed to reset password: " + e.getMessage()));
        }
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 4) {
            return "****";
        }
        return "****" + mobile.substring(mobile.length() - 4);
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String username;
        private String role;
        private String fullName;
        private Long userId;
    }
}