package com.civiceye.model;

import java.time.Instant;

public class FollowUpRecord {
    private String id;
    private Instant scheduledFor;
    private Instant sentAt;
    private String providerMessageId;
    private String status; // SCHEDULED, SENT, DELIVERED, FAILED
    private String messageContent;

    public FollowUpRecord() {
        this.sentAt = Instant.now();
    }

    public FollowUpRecord(String id, Instant scheduledFor, String messageContent) {
        this.id = id;
        this.scheduledFor = scheduledFor;
        this.messageContent = messageContent;
        this.status = "SCHEDULED";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Instant getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(Instant scheduledFor) { this.scheduledFor = scheduledFor; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public String getProviderMessageId() { return providerMessageId; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
}
