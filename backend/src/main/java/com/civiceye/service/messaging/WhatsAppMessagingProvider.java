package com.civiceye.service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * WhatsApp Cloud API Provider.
 */
@Component
public class WhatsAppMessagingProvider implements MessagingProvider {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppMessagingProvider.class);

    @Value("${civiceye.messaging.whatsapp.token:}")
    private String apiToken;

    @Value("${civiceye.messaging.whatsapp.phone-number-id:}")
    private String phoneNumberId;

    private final RestTemplate restTemplate;

    public WhatsAppMessagingProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getProviderName() {
        return "WhatsApp Business Cloud Platform";
    }

    @Override
    public boolean isConfigured() {
        return apiToken != null && !apiToken.isBlank() && !apiToken.startsWith("your_")
                && phoneNumberId != null && !phoneNumberId.isBlank();
    }

    @Override
    public MessageSendResult sendMessage(String toPhone, String textContent, String reportId) {
        if (!isConfigured()) {
            return MessageSendResult.failure(getProviderName(), textContent, "WhatsApp credentials are not configured.");
        }

        String cleanPhone = toPhone.replaceAll("[^0-9]", "");
        String url = "https://graph.facebook.com/v20.0/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", cleanPhone);
        body.put("type", "text");
        body.put("text", Map.of("body", textContent));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("WhatsApp message dispatched successfully to {}", cleanPhone);
                return MessageSendResult.success("WA-MSG-" + System.currentTimeMillis(), getProviderName(), false, false, textContent);
            }
            return MessageSendResult.failure(getProviderName(), textContent, "WhatsApp API HTTP " + response.getStatusCode());
        } catch (Exception ex) {
            log.error("Failed to send WhatsApp message: {}", ex.getMessage());
            return MessageSendResult.failure(getProviderName(), textContent, ex.getMessage());
        }
    }
}
