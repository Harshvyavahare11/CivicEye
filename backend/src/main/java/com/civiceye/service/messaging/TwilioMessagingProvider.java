package com.civiceye.service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Twilio SMS Messaging Adapter.
 */
@Component
public class TwilioMessagingProvider implements MessagingProvider {

    private static final Logger log = LoggerFactory.getLogger(TwilioMessagingProvider.class);

    @Value("${civiceye.messaging.twilio.account-sid:}")
    private String accountSid;

    @Value("${civiceye.messaging.twilio.auth-token:}")
    private String authToken;

    @Value("${civiceye.messaging.twilio.from-number:}")
    private String fromNumber;

    private final RestTemplate restTemplate;

    public TwilioMessagingProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getProviderName() {
        return "Twilio SMS Gateway";
    }

    @Override
    public boolean isConfigured() {
        return accountSid != null && !accountSid.isBlank() && !accountSid.startsWith("your_")
                && authToken != null && !authToken.isBlank() && !authToken.startsWith("your_")
                && fromNumber != null && !fromNumber.isBlank();
    }

    @Override
    public MessageSendResult sendMessage(String toPhone, String textContent, String reportId) {
        if (!isConfigured()) {
            return MessageSendResult.failure(getProviderName(), textContent, "Twilio credentials are not configured in application properties.");
        }

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = accountSid + ":" + authToken;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        headers.set("Authorization", "Basic " + new String(encodedAuth));

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("To", toPhone);
        map.add("From", fromNumber);
        map.add("Body", textContent);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<?, ?> body = response.getBody();
                String sid = (String) body.get("sid");
                String status = (String) body.get("status");
                log.info("Twilio dispatched SMS to {} with SID: {} (status: {})", toPhone, sid, status);
                return MessageSendResult.success(sid, getProviderName(), "delivered".equalsIgnoreCase(status), false, textContent);
            }
            return MessageSendResult.failure(getProviderName(), textContent, "Twilio error: HTTP " + response.getStatusCode());
        } catch (Exception ex) {
            log.error("Failed to send message via Twilio: {}", ex.getMessage());
            return MessageSendResult.failure(getProviderName(), textContent, ex.getMessage());
        }
    }
}
