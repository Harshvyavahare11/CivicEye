package com.civiceye.service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Development & test messaging provider used when live Twilio or WhatsApp credentials are unset.
 * Logs the message and explicitly flags results as simulated.
 */
@Component
public class DevMockMessagingProvider implements MessagingProvider {

    private static final Logger log = LoggerFactory.getLogger(DevMockMessagingProvider.class);

    @Override
    public String getProviderName() {
        return "Development Simulation Adapter (Mock)";
    }

    @Override
    public boolean isConfigured() {
        return true; // Always available as local development fallback
    }

    @Override
    public MessageSendResult sendMessage(String toPhone, String textContent, String reportId) {
        String mockId = "MOCK-MSG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[MOCK MESSAGING] Simulating dispatch to {} for Report {}: [Message ID: {}]\n{}",
                toPhone, reportId, mockId, textContent);

        // Explicitly set isSimulated = true so the UI never misleads the user
        return MessageSendResult.success(
                mockId,
                getProviderName(),
                false, // not confirmed delivered by real carrier
                true,  // simulated
                textContent
        );
    }
}
