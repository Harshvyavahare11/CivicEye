package com.civiceye.model;

import java.time.Instant;

public class InboundMessageRecord {
    private String id;
    private String senderPhone;
    private String messageBody;
    private String parsedKeyword; // STATUS, FIXED, NOT_FIXED, HELP, UNKNOWN
    private Instant receivedAt;
    private String providerEventId;
    private String resultingAction;

    public InboundMessageRecord() {
        this.receivedAt = Instant.now();
    }

    public InboundMessageRecord(String id, String senderPhone, String messageBody, String parsedKeyword, String resultingAction) {
        this.id = id;
        this.senderPhone = senderPhone;
        this.messageBody = messageBody;
        this.parsedKeyword = parsedKeyword;
        this.resultingAction = resultingAction;
        this.receivedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }

    public String getMessageBody() { return messageBody; }
    public void setMessageBody(String messageBody) { this.messageBody = messageBody; }

    public String getParsedKeyword() { return parsedKeyword; }
    public void setParsedKeyword(String parsedKeyword) { this.parsedKeyword = parsedKeyword; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }

    public String getProviderEventId() { return providerEventId; }
    public void setProviderEventId(String providerEventId) { this.providerEventId = providerEventId; }

    public String getResultingAction() { return resultingAction; }
    public void setResultingAction(String resultingAction) { this.resultingAction = resultingAction; }
}
