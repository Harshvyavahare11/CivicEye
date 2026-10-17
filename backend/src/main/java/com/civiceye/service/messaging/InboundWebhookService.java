package com.civiceye.service.messaging;

import com.civiceye.model.EscalationRecord;
import com.civiceye.model.InboundMessageRecord;
import com.civiceye.model.Report;
import com.civiceye.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InboundWebhookService {

    private static final Logger log = LoggerFactory.getLogger(InboundWebhookService.class);

    private final ReportRepository reportRepository;
    private final MessagingService messagingService;

    @Value("${civiceye.escalation.recipient:}")
    private String escalationRecipient;

    // In-memory set to prevent duplicate webhook processing
    private final Set<String> processedMessageIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
            "^(STATUS|FIXED|NOT_FIXED|HELP)(?:\\s+([A-Za-z0-9\\-]+))?",
            Pattern.CASE_INSENSITIVE
    );

    public InboundWebhookService(ReportRepository reportRepository, MessagingService messagingService) {
        this.reportRepository = reportRepository;
        this.messagingService = messagingService;
    }

    public record InboundProcessResult(
            boolean success,
            String reportId,
            String actionTaken,
            String replyMessage,
            String status
    ) {}

    public InboundProcessResult processInboundMessage(com.civiceye.dto.WebhookMessageDto dto) {
        if (dto == null) {
            return new InboundProcessResult(false, null, "EMPTY_PAYLOAD", "Payload is null", "UNKNOWN");
        }
        return processInboundMessage(dto.from(), dto.body(), dto.messageId(), dto.reportId());
    }

    public InboundProcessResult processInboundMessage(String fromPhone, String rawBody, String messageId, String explicitReportId) {
        if (rawBody == null || rawBody.isBlank()) {
            return new InboundProcessResult(false, null, "IGNORED_EMPTY", "Message body is empty", "UNKNOWN");
        }

        // Deduplication
        if (messageId != null && !messageId.isBlank()) {
            if (processedMessageIds.contains(messageId)) {
                log.warn("Duplicate webhook event ignored: ID={}", messageId);
                return new InboundProcessResult(true, null, "DUPLICATE_IGNORED", "Event already processed", "DUPLICATE");
            }
            processedMessageIds.add(messageId);
        }

        String trimmed = rawBody.trim();
        Matcher matcher = KEYWORD_PATTERN.matcher(trimmed);

        String keyword = "UNKNOWN";
        String extractedId = explicitReportId;

        if (matcher.find()) {
            keyword = matcher.group(1).toUpperCase();
            if (extractedId == null || extractedId.isBlank()) {
                extractedId = matcher.group(2);
            }
        }

        // Handle HELP
        if ("HELP".equals(keyword)) {
            String helpText = "CivicEye SMS/WhatsApp Bot Commands:\n• STATUS <ID> - Check report status\n• FIXED <ID> - Confirm issue is resolved\n• NOT_FIXED <ID> - Escalate unresolved issue\nExample: FIXED CE-1001";
            return new InboundProcessResult(true, null, "HELP", helpText, "OK");
        }

        // If report ID is missing, try matching reports by sender phone
        if (extractedId == null || extractedId.isBlank()) {
            List<Report> activeUserReports = reportRepository.findAll().stream()
                    .filter(r -> fromPhone != null && fromPhone.equals(r.getRecipientPhone()))
                    .filter(r -> !"Resolved".equalsIgnoreCase(r.getStatus()) && !"Closed".equalsIgnoreCase(r.getStatus()))
                    .toList();

            if (activeUserReports.isEmpty()) {
                return new InboundProcessResult(
                        false,
                        null,
                        "REPORT_NOT_FOUND",
                        "Please specify the Report ID in your command (e.g. " + keyword + " CE-1001).",
                        "AMBIGUOUS"
                );
            } else if (activeUserReports.size() > 1) {
                return new InboundProcessResult(
                        false,
                        null,
                        "MULTIPLE_REPORTS",
                        "Multiple active reports found for this number. Please specify the Report ID: " + keyword + " <ID>.",
                        "AMBIGUOUS"
                );
            } else {
                extractedId = activeUserReports.get(0).getId();
            }
        }

        // Fetch target report
        Optional<Report> reportOpt = reportRepository.findById(extractedId.trim());
        if (reportOpt.isEmpty()) {
            log.warn("Inbound command for non-existent report ID: {}", extractedId);
            return new InboundProcessResult(
                    false,
                    extractedId,
                    "REPORT_NOT_FOUND",
                    "Report ID '" + extractedId + "' was not found in CivicEye.",
                    "NOT_FOUND"
            );
        }

        Report report = reportOpt.get();
        String actionTaken;
        String responseText;

        switch (keyword) {
            case "STATUS" -> {
                actionTaken = "STATUS_QUERY";
                responseText = "CivicEye Report Status: " + report.getId() + " is currently '" + report.getStatus()
                        + "'. Category: " + report.getCategory() + " | Department: " + (report.getSuggestedDepartment() != null ? report.getSuggestedDepartment() : "Assigned")
                        + " | Last Update: " + report.getUpdatedAt();
            }
            case "FIXED" -> {
                actionTaken = "MARKED_FIXED";
                report.setCitizenConfirmedResolved(true);
                report.setStatus("Awaiting Citizen Confirmation");
                report.setCommunicationStatus("Delivered");
                report.setUpdatedAt(Instant.now());
                responseText = "Thank you! Your confirmation that " + report.getId() + " has been fixed has been recorded.";
                log.info("Report {} confirmed FIXED by citizen.", report.getId());
            }
            case "NOT_FIXED" -> {
                actionTaken = "ESCALATED";
                report.setStatus("Escalated");
                report.setUpdatedAt(Instant.now());

                // Trigger escalation workflow
                triggerEscalation(report, "Citizen reported issue as NOT_FIXED via two-way reply.");
                responseText = "Notice received: Issue " + report.getId() + " has been ESCALATED to higher municipal authorities for urgent intervention.";
                log.info("Report {} escalated following NOT_FIXED reply.", report.getId());
            }
            default -> {
                actionTaken = "UNRECOGNIZED_COMMAND";
                responseText = "Unrecognized keyword. Reply with STATUS " + report.getId() + ", FIXED " + report.getId() + ", or NOT_FIXED " + report.getId() + ".";
            }
        }

        // Record inbound message
        InboundMessageRecord record = new InboundMessageRecord(
                "INB-" + UUID.randomUUID().toString().substring(0, 8),
                fromPhone != null ? fromPhone : "unknown",
                rawBody,
                keyword,
                actionTaken
        );
        record.setProviderEventId(messageId);
        report.getInboundMessages().add(record);

        reportRepository.save(report);

        return new InboundProcessResult(true, report.getId(), actionTaken, responseText, report.getStatus());
    }

    public void triggerEscalation(Report report, String reason) {
        String targetContact = (escalationRecipient != null && !escalationRecipient.isBlank())
                ? escalationRecipient.trim()
                : null;

        String escalationStatus;
        if (targetContact != null) {
            escalationStatus = "NOTIFIED";
            messagingService.sendEscalationMessage(report, targetContact, reason);
        } else {
            escalationStatus = "PENDING_CONFIGURATION";
            log.warn("Escalation triggered for Report {} but no civiceye.escalation.recipient is configured.", report.getId());
        }

        EscalationRecord record = new EscalationRecord(
                "ESC-" + UUID.randomUUID().toString().substring(0, 8),
                reason,
                targetContact != null ? targetContact : "Unconfigured Authority",
                escalationStatus
        );
        report.getEscalationHistory().add(record);
        report.setStatus("Escalated");
        report.setUpdatedAt(Instant.now());
        reportRepository.save(report);
    }
}
