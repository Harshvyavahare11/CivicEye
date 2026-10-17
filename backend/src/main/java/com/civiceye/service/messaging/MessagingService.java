package com.civiceye.service.messaging;

import com.civiceye.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessagingService {

    private static final Logger log = LoggerFactory.getLogger(MessagingService.class);

    @Value("${civiceye.messaging.default-test-recipient:+919876543210}")
    private String defaultTestRecipient;

    private final List<MessagingProvider> providers;
    private final DevMockMessagingProvider devMockProvider;

    public MessagingService(List<MessagingProvider> providers, DevMockMessagingProvider devMockProvider) {
        this.providers = providers;
        this.devMockProvider = devMockProvider;
    }

    public MessagingProvider getActiveProvider() {
        for (MessagingProvider provider : providers) {
            if (provider != devMockProvider && provider.isConfigured()) {
                return provider;
            }
        }
        return devMockProvider;
    }

    public MessageSendResult sendComplaintMessage(Report report, String recipientPhone) {
        String targetPhone = (recipientPhone != null && !recipientPhone.isBlank())
                ? recipientPhone.trim()
                : defaultTestRecipient;

        String formattedMessage = formatComplaintMessage(report);
        MessagingProvider activeProvider = getActiveProvider();
        log.info("Sending complaint for Report {} via {} to {}", report.getId(), activeProvider.getProviderName(), targetPhone);

        return activeProvider.sendMessage(targetPhone, formattedMessage, report.getId());
    }

    public MessageSendResult sendFollowUpMessage(Report report) {
        String targetPhone = (report.getRecipientPhone() != null && !report.getRecipientPhone().isBlank())
                ? report.getRecipientPhone()
                : defaultTestRecipient;

        String followUpText = formatFollowUpMessage(report);
        MessagingProvider activeProvider = getActiveProvider();
        log.info("Sending scheduled follow-up for Report {} via {} to {}", report.getId(), activeProvider.getProviderName(), targetPhone);

        return activeProvider.sendMessage(targetPhone, followUpText, report.getId());
    }

    public MessageSendResult sendEscalationMessage(Report report, String escalationContact, String reason) {
        String escalationText = formatEscalationMessage(report, reason);
        MessagingProvider activeProvider = getActiveProvider();
        log.info("Sending escalation alert for Report {} via {} to {}", report.getId(), activeProvider.getProviderName(), escalationContact);

        return activeProvider.sendMessage(escalationContact, escalationText, report.getId());
    }

    public String formatComplaintMessage(Report report) {
        StringBuilder sb = new StringBuilder();
        sb.append("📢 [CivicEye Grievance: ").append(report.getId()).append("]\n");
        sb.append("Title: ").append(report.getTitle()).append("\n");
        sb.append("Category: ").append(report.getCategory()).append(" | Severity: ").append(report.getSeverity()).append("\n");
        sb.append("Location: ").append(report.getLocation() != null ? report.getLocation() : "Unspecified");

        if (report.getLatitude() != null && report.getLongitude() != null) {
            sb.append(" (https://maps.google.com/?q=").append(report.getLatitude()).append(",").append(report.getLongitude()).append(")");
        }
        sb.append("\n");

        if (report.getSuggestedDepartment() != null && !report.getSuggestedDepartment().isBlank()) {
            sb.append("Assigned Authority: ").append(report.getSuggestedDepartment()).append("\n");
        }

        sb.append("\nComplaint Draft:\n\"").append(report.getComplaintText()).append("\"\n\n");
        sb.append("Supported Reply Actions:\n");
        sb.append("• STATUS ").append(report.getId()).append(" : Check tracking status\n");
        sb.append("• FIXED ").append(report.getId()).append(" : Confirm issue is resolved\n");
        sb.append("• NOT_FIXED ").append(report.getId()).append(" : Escalate unresolved grievance\n");
        sb.append("• HELP : View reply commands");
        return sb.toString();
    }

    public String formatFollowUpMessage(Report report) {
        return "⏰ [CivicEye Follow-Up: " + report.getId() + "]\n"
                + "Regarding your grievance: \"" + report.getTitle() + "\" (" + report.getCategory() + " at " + report.getLocation() + ").\n\n"
                + "Has this civic infrastructure issue been resolved?\n"
                + "👉 Reply 'FIXED " + report.getId() + "' if it has been fixed.\n"
                + "👉 Reply 'NOT_FIXED " + report.getId() + "' if it remains unresolved (will trigger escalation).";
    }

    public String formatEscalationMessage(Report report, String reason) {
        return "🚨 [CivicEye Escalation Notice: " + report.getId() + "]\n"
                + "ATTENTION: Civic issue has been ESCALATED.\n"
                + "Reason: " + reason + "\n"
                + "Issue: " + report.getTitle() + " (" + report.getCategory() + ")\n"
                + "Location: " + report.getLocation() + "\n"
                + "Severity: " + report.getSeverity() + "\n"
                + "Original Grievance: \"" + report.getComplaintText() + "\"\n"
                + "Immediate inspection and official intervention required.";
    }

    public String getDefaultTestRecipient() {
        return defaultTestRecipient;
    }
}
