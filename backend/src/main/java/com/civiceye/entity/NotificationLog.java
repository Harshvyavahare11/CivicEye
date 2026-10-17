package com.civiceye.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_logs", indexes = {
        @Index(name = "idx_notification_logs_report_id", columnList = "report_id"),
        @Index(name = "idx_notification_logs_report_number", columnList = "report_number"),
        @Index(name = "idx_notification_logs_outcome", columnList = "outcome"),
        @Index(name = "idx_notification_logs_idempotency", columnList = "idempotency_key", unique = true)
})
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = true)
    @JsonIgnore
    private CivicReport report;

    @Column(name = "report_number", length = 50)
    private String reportNumber;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 150)
    private String idempotencyKey;

    @Column(name = "recipient_masked", nullable = false, length = 50)
    private String recipientMasked;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_message_sid", length = 100)
    private String providerMessageSid;

    @Column(name = "outcome", nullable = false, length = 30)
    private String outcome;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "last_attempted_at", nullable = false)
    private Instant lastAttemptedAt = Instant.now();

    public NotificationLog() {
    }

    public NotificationLog(
            CivicReport report,
            String reportNumber,
            String eventType,
            String idempotencyKey,
            String recipientMasked,
            String provider,
            String providerMessageSid,
            String outcome,
            String errorCode
    ) {
        this.report = report;
        this.reportNumber = reportNumber;
        this.eventType = eventType;
        this.idempotencyKey = idempotencyKey;
        this.recipientMasked = recipientMasked;
        this.provider = provider;
        this.providerMessageSid = providerMessageSid;
        this.outcome = outcome;
        this.errorCode = errorCode;
        this.attemptCount = 1;
        this.createdAt = Instant.now();
        this.lastAttemptedAt = Instant.now();
    }

    public static String maskPhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) return "UNKNOWN";
        String clean = phone.trim();
        if (clean.length() <= 4) return "****";
        int visibleChars = Math.min(4, clean.length() / 2);
        String prefix = clean.substring(0, Math.min(3, clean.length() - visibleChars));
        String suffix = clean.substring(clean.length() - visibleChars);
        return prefix + "****" + suffix;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CivicReport getReport() {
        return report;
    }

    public void setReport(CivicReport report) {
        this.report = report;
    }

    public String getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(String reportNumber) {
        this.reportNumber = reportNumber;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getRecipientMasked() {
        return recipientMasked;
    }

    public void setRecipientMasked(String recipientMasked) {
        this.recipientMasked = recipientMasked;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderMessageSid() {
        return providerMessageSid;
    }

    public void setProviderMessageSid(String providerMessageSid) {
        this.providerMessageSid = providerMessageSid;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastAttemptedAt() {
        return lastAttemptedAt;
    }

    public void setLastAttemptedAt(Instant lastAttemptedAt) {
        this.lastAttemptedAt = lastAttemptedAt;
    }
}
