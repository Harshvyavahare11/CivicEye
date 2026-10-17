package com.civiceye.service.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Official Twilio Programmable Messaging SMS Adapter.
 * Dispatches transactional notifications via Twilio REST API.
 */
@Component
public class TwilioMessagingProvider implements MessagingProvider {

    private static final Logger log = LoggerFactory.getLogger(TwilioMessagingProvider.class);
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    @Value("${civiceye.messaging.twilio.account-sid:}")
    private String accountSid;

    @Value("${civiceye.messaging.twilio.auth-token:}")
    private String authToken;

    @Value("${civiceye.messaging.twilio.from-number:}")
    private String fromNumber;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                && fromNumber != null && !fromNumber.isBlank() && !fromNumber.startsWith("your_");
    }

    @Override
    public MessageSendResult sendMessage(String toPhone, String textContent, String reportId) {
        if (!isConfigured()) {
            log.warn("Twilio SMS dispatch skipped: missing or placeholder credentials");
            return MessageSendResult.failure(getProviderName(), textContent,
                    "Twilio credentials (TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_PHONE) are not configured.",
                    "TWILIO_NOT_CONFIGURED");
        }

        if (toPhone == null || !E164_PATTERN.matcher(toPhone.trim()).matches()) {
            log.warn("Invalid recipient phone format: {}", toPhone);
            return MessageSendResult.failure(getProviderName(), textContent,
                    "Recipient phone number must be in valid E.164 format (e.g. +917058207967).",
                    "INVALID_E164_PHONE");
        }

        String cleanTo = toPhone.trim();
        String cleanFrom = fromNumber.trim();

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid.trim() + "/Messages.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // HTTP Basic Auth: AccountSid:AuthToken
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                (accountSid.trim() + ":" + authToken.trim()).getBytes(StandardCharsets.UTF_8)
        );
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("To", cleanTo);
        form.add("From", cleanFrom);
        form.add("Body", textContent);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        String maskedTo = maskPhone(cleanTo);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String sid = root.path("sid").asText();
                String status = root.path("status").asText();

                log.info("Twilio accepted SMS for report [{}] to {} with SID: {} (status: {})",
                        reportId, maskedTo, sid, status);

                // Twilio response acceptance (queued/sent) is not proof of carrier delivery
                return MessageSendResult.success(sid, getProviderName(), false, false, textContent);
            }

            return MessageSendResult.failure(getProviderName(), textContent,
                    "Unexpected response HTTP " + response.getStatusCode(), "UNEXPECTED_HTTP_STATUS");

        } catch (HttpStatusCodeException ex) {
            String errorCode = "TWILIO_HTTP_" + ex.getStatusCode().value();
            String errorMessage = "Twilio request failed with HTTP " + ex.getStatusCode().value();

            try {
                String responseBody = ex.getResponseBodyAsString();
                if (responseBody != null && !responseBody.isBlank()) {
                    JsonNode errorNode = objectMapper.readTree(responseBody);
                    if (errorNode.has("code")) {
                        errorCode = "TWILIO_ERR_" + errorNode.path("code").asText();
                    }
                    if (errorNode.has("message")) {
                        errorMessage = errorNode.path("message").asText();
                    }
                }
            } catch (Exception parseEx) {
                // Keep default error message
            }

            log.error("Twilio API error sending to {}: code={}, message={}", maskedTo, errorCode, errorMessage);
            return MessageSendResult.failure(getProviderName(), textContent, errorMessage, errorCode);

        } catch (ResourceAccessException ex) {
            log.error("Twilio connection timeout or network failure sending to {}: {}", maskedTo, ex.getMessage());
            return MessageSendResult.failure(getProviderName(), textContent,
                    "Twilio connection timed out or network unreachable: " + ex.getMessage(), "NETWORK_TIMEOUT");

        } catch (Exception ex) {
            log.error("Unexpected error dispatching Twilio SMS: {}", ex.getMessage());
            return MessageSendResult.failure(getProviderName(), textContent, ex.getMessage(), "INTERNAL_ERROR");
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) return "****";
        return phone.substring(0, Math.min(3, phone.length() - 4)) + "****" + phone.substring(phone.length() - 4);
    }
}
