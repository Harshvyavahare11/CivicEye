package com.civiceye.service.messaging;

/**
 * Common adapter interface for external messaging providers (Twilio, WhatsApp Cloud API, or Dev Mock).
 */
public interface MessagingProvider {

    String getProviderName();

    boolean isConfigured();

    MessageSendResult sendMessage(String toPhone, String textContent, String reportId);
}
