package com.civiceye.controller;

import com.civiceye.dto.WebhookMessageDto;
import com.civiceye.service.messaging.InboundWebhookService;
import com.civiceye.service.messaging.InboundWebhookService.InboundProcessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/messaging")
public class MessagingWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MessagingWebhookController.class);
    private final InboundWebhookService webhookService;

    public MessagingWebhookController(InboundWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * Webhook verification for Meta WhatsApp Cloud API.
     * When Meta sets up webhooks, it issues a GET request with hub.mode, hub.verify_token, hub.challenge.
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(value = "hub.challenge", required = false) String challenge
    ) {
        log.info("Received webhook verification request: mode={}, token={}", mode, verifyToken);
        if ("subscribe".equals(mode) && challenge != null) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.ok("CivicEye Webhook endpoint is active");
    }

    /**
     * Inbound JSON message webhook (WhatsApp, generic webhooks).
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handleJsonWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received JSON webhook payload: {}", payload);
        WebhookMessageDto dto = extractDto(payload);
        InboundProcessResult result = webhookService.processInboundMessage(dto);
        return ResponseEntity.ok(toMap(result));
    }

    /**
     * Inbound form-urlencoded webhook (standard Twilio SMS webhook format: From, Body, MessageSid).
     */
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleFormWebhook(@RequestParam Map<String, String> formData) {
        log.info("Received Form webhook payload: {}", formData);
        String from = formData.getOrDefault("From", "");
        String body = formData.getOrDefault("Body", "");
        String messageSid = formData.getOrDefault("MessageSid", "TW-" + System.currentTimeMillis());

        InboundProcessResult result = webhookService.processInboundMessage(from, body, messageSid, null);

        // Twilio expects TwiML XML or plain text
        String replyText = result.replyMessage();
        String twiml = "<Response><Message>" + escapeXml(replyText) + "</Message></Response>";
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(twiml);
    }

    /**
     * Simulation endpoint for local hackathon testing and demo UI.
     * Allows testing reply keywords (STATUS, FIXED, NOT_FIXED, HELP) directly from the browser.
     */
    @PostMapping("/simulate")
    public ResponseEntity<Map<String, Object>> simulateInboundMessage(
            @RequestBody Map<String, String> simRequest
    ) {
        String from = simRequest.getOrDefault("from", "+15551234567");
        String body = simRequest.getOrDefault("body", "HELP");
        String reportId = simRequest.get("reportId");
        String eventId = "SIM-" + System.currentTimeMillis();

        InboundProcessResult result = webhookService.processInboundMessage(from, body, eventId, reportId);
        return ResponseEntity.ok(toMap(result));
    }

    private Map<String, Object> toMap(InboundProcessResult result) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", result.success());
        map.put("reportId", result.reportId());
        map.put("action", result.actionTaken());
        map.put("replyText", result.replyMessage());
        map.put("newStatus", result.status());
        return map;
    }

    private WebhookMessageDto extractDto(Map<String, Object> payload) {
        // WhatsApp Cloud API payload inspection
        if (payload.containsKey("entry")) {
            try {
                var entries = (java.util.List<?>) payload.get("entry");
                if (entries != null && !entries.isEmpty()) {
                    var entry = (Map<?, ?>) entries.get(0);
                    var changes = (java.util.List<?>) entry.get("changes");
                    if (changes != null && !changes.isEmpty()) {
                        var change = (Map<?, ?>) changes.get(0);
                        var value = (Map<?, ?>) change.get("value");
                        var messages = (java.util.List<?>) value.get("messages");
                        if (messages != null && !messages.isEmpty()) {
                            var message = (Map<?, ?>) messages.get(0);
                            String from = (String) message.get("from");
                            String id = (String) message.get("id");
                            var textObj = (Map<?, ?>) message.get("text");
                            String body = textObj != null ? (String) textObj.get("body") : "";
                            return new WebhookMessageDto(from, body, id, null);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse WhatsApp Cloud API structure, falling back to top-level: {}", e.getMessage());
            }
        }

        String from = (String) payload.getOrDefault("from", payload.getOrDefault("From", "unknown"));
        String body = (String) payload.getOrDefault("body", payload.getOrDefault("Body", payload.getOrDefault("text", "")));
        String eventId = (String) payload.getOrDefault("eventId", payload.getOrDefault("MessageSid", payload.getOrDefault("id", "EVT-" + System.currentTimeMillis())));
        String reportId = (String) payload.get("reportId");

        return new WebhookMessageDto(from, body, eventId, reportId);
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
