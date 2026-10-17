package com.civiceye.dto;

public record WebhookMessageDto(
        String from,
        String body,
        String messageId,
        String reportId
) {}
