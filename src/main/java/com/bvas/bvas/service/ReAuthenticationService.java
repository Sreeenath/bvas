package com.bvas.bvas.service;

import com.bvas.bvas.exception.ReAuthenticationRequiredException;
import com.bvas.bvas.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ReAuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final DummyOtpService otpService;
    
    // Store re-authentication sessions (use Redis in production)
    private final Map<String, ReAuthSession> reAuthSessions = new ConcurrentHashMap<>();
    private static final long REAUTH_EXPIRY_SECONDS = 300; // 5 minutes

    public void validateReAuthentication(User user, String password, String otp) {
        // Validate password
        if (password != null && !password.isEmpty()) {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new ReAuthenticationRequiredException("Invalid password for re-authentication");
            }
        }
        
        // Validate OTP if provided
        if (otp != null && !otp.isEmpty()) {
            String identifier = user.getUsername() + "_reauth";
            if (!otpService.validateOtp(identifier, otp)) {
                throw new ReAuthenticationRequiredException("Invalid OTP for re-authentication");
            }
        }
        
        // Create re-auth session
        String sessionId = user.getId() + "_" + System.currentTimeMillis();
        ReAuthSession session = new ReAuthSession(user.getId(), LocalDateTime.now().plusSeconds(REAUTH_EXPIRY_SECONDS));
        reAuthSessions.put(sessionId, session);
    }

    public boolean isReAuthenticated(Long userId, String sessionId) {
        ReAuthSession session = reAuthSessions.get(sessionId);
        if (session == null) {
            return false;
        }
        
        if (!session.getUserId().equals(userId)) {
            return false;
        }
        
        if (session.getExpiryTime().isBefore(LocalDateTime.now())) {
            reAuthSessions.remove(sessionId);
            return false;
        }
        
        return true;
    }

    public String generateReAuthOtp(User user) {
        String identifier = user.getUsername() + "_reauth";
        return otpService.generateOtp(identifier);
    }

    private static class ReAuthSession {
        private final Long userId;
        private final LocalDateTime expiryTime;

        public ReAuthSession(Long userId, LocalDateTime expiryTime) {
            this.userId = userId;
            this.expiryTime = expiryTime;
        }

        public Long getUserId() {
            return userId;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }
}