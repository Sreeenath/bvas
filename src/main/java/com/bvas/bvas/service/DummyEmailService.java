package com.bvas.bvas.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * DUMMY Email Service for Local Development Only
 * This service simulates email sending without actual SMTP integration.
 * In production, this should be replaced with actual email service (e.g., SendGrid, AWS SES, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DummyEmailService {

    @Value("${email.dummy.enabled:true}")
    private Boolean dummyModeEnabled;

    @Value("${email.dummy.save-to-file:false}")
    private Boolean saveToFile;

    // In-memory storage for sent emails (for viewing in logs/UI)
    private final Map<String, List<EmailRecord>> emailStore = new ConcurrentHashMap<>();

    /**
     * Send email (dummy implementation)
     */
    public void sendEmail(String to, String subject, String body, EmailType emailType) {
        if (!dummyModeEnabled) {
            log.warn("Email service is disabled. Would send email to: {}", to);
            return;
        }

        EmailRecord email = new EmailRecord(to, subject, body, emailType, LocalDateTime.now());
        
        // Store email
        emailStore.computeIfAbsent(to, k -> new ArrayList<>()).add(email);
        
        // Log email
        logEmail(email);
        
        // Optionally save to file if configured
        if (saveToFile) {
            saveEmailToFile(email);
        }
    }

    /**
     * Send user registration welcome email
     */
    public void sendRegistrationEmail(String to, String username, String fullName) {
        String subject = "Welcome to BVAS - Registration Successful";
        String body = buildRegistrationEmailBody(username, fullName);
        sendEmail(to, subject, body, EmailType.REGISTRATION);
    }

    /**
     * Send user approval email
     */
    public void sendApprovalEmail(String to, String username, String fullName, String role) {
        String subject = "BVAS Account Approved - You can now login";
        String body = buildApprovalEmailBody(username, fullName, role);
        sendEmail(to, subject, body, EmailType.APPROVAL);
    }

    /**
     * Send user rejection email
     */
    public void sendRejectionEmail(String to, String username, String fullName, String reason) {
        String subject = "BVAS Registration - Account Not Approved";
        String body = buildRejectionEmailBody(username, fullName, reason);
        sendEmail(to, subject, body, EmailType.REJECTION);
    }

    /**
     * Send new user created email (when admin creates user)
     */
    public void sendUserCreatedEmail(String to, String username, String password, String fullName, String role) {
        String subject = "BVAS Account Created - Your Login Credentials";
        String body = buildUserCreatedEmailBody(username, password, fullName, role);
        sendEmail(to, subject, body, EmailType.USER_CREATED);
    }

    /**
     * Send password reset email (for future use)
     */
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        String subject = "BVAS Password Reset Request";
        String body = buildPasswordResetEmailBody(username, resetToken);
        sendEmail(to, subject, body, EmailType.PASSWORD_RESET);
    }

    /**
     * Get all emails sent to a specific address
     */
    public List<EmailRecord> getEmailsFor(String emailAddress) {
        return emailStore.getOrDefault(emailAddress, new ArrayList<>());
    }

    /**
     * Get all sent emails (for admin viewing)
     */
    public Map<String, List<EmailRecord>> getAllEmails() {
        return new ConcurrentHashMap<>(emailStore);
    }

    private void logEmail(EmailRecord email) {
        log.warn("═══════════════════════════════════════════════════════════");
        log.warn("DUMMY EMAIL SENT (Local Development Mode)");
        log.warn("═══════════════════════════════════════════════════════════");
        log.warn("To: {}", email.getTo());
        log.warn("Subject: {}", email.getSubject());
        log.warn("Type: {}", email.getEmailType());
        log.warn("Sent At: {}", email.getSentAt());
        log.warn("───────────────────────────────────────────────────────────");
        log.warn("Body:");
        log.warn(email.getBody());
        log.warn("═══════════════════════════════════════════════════════════");
        log.warn("In production, this email would be sent via SMTP/Email Service");
        log.warn("═══════════════════════════════════════════════════════════");
    }

    private void saveEmailToFile(EmailRecord email) {
        // TODO: Implement file saving if needed
        // Could save to ./emails/{timestamp}_{to}.txt
    }

    // Email Body Builders
    private String buildRegistrationEmailBody(String username, String fullName) {
        return String.format("""
            Dear %s,
            
            Thank you for registering with the Bill Verification and Approval System (BVAS).
            
            Your registration has been received and is pending approval from the HQ Administrator.
            
            Registration Details:
            - Username: %s
            - Full Name: %s
            
            You will receive another email once your account has been reviewed and approved.
            
            Please do not reply to this email. If you have any questions, please contact the system administrator.
            
            Best regards,
            BVAS System
            """, fullName, username, fullName);
    }

    private String buildApprovalEmailBody(String username, String fullName, String role) {
        return String.format("""
            Dear %s,
            
            Great news! Your BVAS account has been approved.
            
            You can now log in to the system using your credentials:
            - Username: %s
            - Role: %s
            
            Please visit: http://localhost:8080/login.html
            
            If you have any questions or need assistance, please contact the system administrator.
            
            Best regards,
            BVAS System
            """, fullName, username, role);
    }

    private String buildRejectionEmailBody(String username, String fullName, String reason) {
        return String.format("""
            Dear %s,
            
            We regret to inform you that your BVAS registration could not be approved at this time.
            
            Registration Details:
            - Username: %s
            - Full Name: %s
            
            Reason: %s
            
            If you believe this is an error or would like to appeal this decision, please contact the system administrator.
            
            Best regards,
            BVAS System
            """, fullName, username, fullName, reason != null ? reason : "Not specified");
    }

    private String buildUserCreatedEmailBody(String username, String password, String fullName, String role) {
        return String.format("""
            Dear %s,
            
            Your BVAS account has been created by the system administrator.
            
            Your login credentials are:
            - Username: %s
            - Password: %s
            - Role: %s
            
            Please log in at: http://localhost:8080/login.html
            
            IMPORTANT: For security reasons, please change your password after your first login.
            
            If you have any questions, please contact the system administrator.
            
            Best regards,
            BVAS System
            """, fullName, username, password, role);
    }

    private String buildPasswordResetEmailBody(String username, String resetToken) {
        return String.format("""
            Dear User,
            
            You have requested to reset your password for your BVAS account.
            
            Username: %s
            
            Reset Token: %s
            
            Please use this token to reset your password. This token will expire in 24 hours.
            
            If you did not request this password reset, please ignore this email.
            
            Best regards,
            BVAS System
            """, username, resetToken);
    }

    /**
     * Email Record for storing sent emails
     */
    public static class EmailRecord {
        private final String to;
        private final String subject;
        private final String body;
        private final EmailType emailType;
        private final LocalDateTime sentAt;

        public EmailRecord(String to, String subject, String body, EmailType emailType, LocalDateTime sentAt) {
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.emailType = emailType;
            this.sentAt = sentAt;
        }

        public String getTo() { return to; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
        public EmailType getEmailType() { return emailType; }
        public LocalDateTime getSentAt() { return sentAt; }
    }

    public void sendPasswordResetConfirmationEmail(String to, String username) {
        String subject = "BVAS Password Reset Successful";
        String body = buildPasswordResetConfirmationEmailBody(username);
        sendEmail(to, subject, body, EmailType.PASSWORD_RESET);
    }

    private String buildPasswordResetConfirmationEmailBody(String username) {
        return String.format("""
            Dear User,
            
            Your password has been successfully reset for your BVAS account.
            
            Username: %s
            
            If you did not perform this action, please contact the system administrator immediately.
            
            Best regards,
            BVAS System
            """, username);
    }

    public enum EmailType {
        REGISTRATION,
        APPROVAL,
        REJECTION,
        USER_CREATED,
        PASSWORD_RESET,
        BILL_APPROVED,
        BILL_REJECTED
    }
}