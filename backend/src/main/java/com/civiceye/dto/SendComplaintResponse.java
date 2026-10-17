package com.civiceye.dto;

import java.time.Instant;

public record SendComplaintResponse(
        String reportId,
        String status, // SENT, FAILED, PENDING_SEND
        String communicationStatus,
        String providerMessageId,
        String recipientPhone,
        String messageSent,
        String providerName,
        boolean isDelivered,
        boolean isSimulated,
        Instant sentAt,
        String note
) {}
