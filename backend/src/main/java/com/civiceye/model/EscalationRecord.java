package com.civiceye.model;

import java.time.Instant;

public class EscalationRecord {
    private String id;
    private Instant escalatedAt;
    private String reason;
    private String targetRecipient;
    private String status; // ESCALATED, NOTIFIED, PENDING_CONFIGURATION
    private String providerMessageId;

    public EscalationRecord() {
        this.escalatedAt = Instant.now();
    }

    public EscalationRecord(String id, String reason, String targetRecipient, String status) {
        this.id = id;
        this.reason = reason;
        this.targetRecipient = targetRecipient;
        this.status = status;
        this.escalatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Instant getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(Instant escalatedAt) { this.escalatedAt = escalatedAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getTargetRecipient() { return targetRecipient; }
    public void setTargetRecipient(String targetRecipient) { this.targetRecipient = targetRecipient; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProviderMessageId() { return providerMessageId; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }
}
