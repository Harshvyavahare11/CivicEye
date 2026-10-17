package com.civiceye.service.messaging;

public record MessageSendResult(
        boolean success,
        String providerMessageId,
        String providerName,
        boolean isDelivered,
        boolean isSimulated,
        String messageBody,
        String errorMessage,
        String errorCode
) {
    public static MessageSendResult success(String providerMessageId, String providerName, boolean isDelivered, boolean isSimulated, String messageBody) {
        return new MessageSendResult(true, providerMessageId, providerName, isDelivered, isSimulated, messageBody, null, null);
    }

    public static MessageSendResult failure(String providerName, String messageBody, String errorMessage) {
        return new MessageSendResult(false, null, providerName, false, false, messageBody, errorMessage, null);
    }

    public static MessageSendResult failure(String providerName, String messageBody, String errorMessage, String errorCode) {
        return new MessageSendResult(false, null, providerName, false, false, messageBody, errorMessage, errorCode);
    }
}
