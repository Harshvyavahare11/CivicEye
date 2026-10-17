package com.civiceye.service.messaging;

import com.civiceye.entity.CivicReport;
import com.civiceye.entity.ReportStatus;
import com.civiceye.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessagingService {

    private static final Logger log = LoggerFactory.getLogger(MessagingService.class);

    @Value("${civiceye.messaging.provider:mock}")
    private String configuredProvider;

    @Value("${civiceye.messaging.default-test-recipient:+919876543210}")
    private String defaultTestRecipient;

    @Value("${civiceye.escalation.recipient:}")
    private String escalationRecipient;

    private final List<MessagingProvider> providers;
    private final DevMockMessagingProvider devMockProvider;

    public MessagingService(List<MessagingProvider> providers, DevMockMessagingProvider devMockProvider) {
        this.providers = providers;
        this.devMockProvider = devMockProvider;
    }

    /**
     * Resolves the active provider strictly honoring CIVICEYE_MESSAGING_PROVIDER.
     * Defaults to mock mode.
     */
    public MessagingProvider getActiveProvider() {
        String mode = configuredProvider != null ? configuredProvider.trim().toLowerCase() : "mock";

        if ("twilio".equals(mode)) {
            for (MessagingProvider provider : providers) {
                if (provider instanceof TwilioMessagingProvider) {
                    return provider;
                }
            }
        } else if ("whatsapp".equals(mode)) {
            for (MessagingProvider provider : providers) {
                if (provider instanceof WhatsAppMessagingProvider) {
                    return provider;
                }
            }
        }

        return devMockProvider;
    }

    // =========================================================================
    // CivicReport (PostgreSQL JPA Entity) Notification Methods
    // =========================================================================

    public MessageSendResult sendReportSubmitted(CivicReport report) {
        if (!shouldNotifyComplainant(report)) {
            return MessageSendResult.failure(getActiveProvider().getProviderName(),
                    "", "Notification skipped: complainant did not provide phone or consent", "NO_CONSENT");
        }

        String recipient = report.getRecipientPhone().trim();
        String body = formatSubmittedMessage(report);
        log.info("Sending submission confirmation for {} to {}", report.getReportNumber(), recipient);
        return getActiveProvider().sendMessage(recipient, body, report.getReportNumber());
    }

    public MessageSendResult sendReportAssigned(CivicReport report, String department) {
        if (!shouldNotifyComplainant(report)) {
            return MessageSendResult.failure(getActiveProvider().getProviderName(),
                    "", "Notification skipped: complainant did not provide phone or consent", "NO_CONSENT");
        }

        String recipient = report.getRecipientPhone().trim();
        String body = formatAssignedMessage(report, department);
        log.info("Sending assignment update for {} to {}", report.getReportNumber(), recipient);
        return getActiveProvider().sendMessage(recipient, body, report.getReportNumber());
    }

    public MessageSendResult sendReportStatusChanged(CivicReport report, ReportStatus oldStatus, ReportStatus newStatus) {
        if (!shouldNotifyComplainant(report)) {
            return MessageSendResult.failure(getActiveProvider().getProviderName(),
                    "", "Notification skipped: complainant did not provide phone or consent", "NO_CONSENT");
        }

        String recipient = report.getRecipientPhone().trim();
        String body = formatStatusChangedMessage(report, oldStatus, newStatus);
        log.info("Sending status update for {} ({}) to {}", report.getReportNumber(), newStatus, recipient);
        return getActiveProvider().sendMessage(recipient, body, report.getReportNumber());
    }

    public MessageSendResult sendFollowUpMessage(CivicReport report) {
        if (!shouldNotifyComplainant(report)) {
            return MessageSendResult.failure(getActiveProvider().getProviderName(),
                    "", "Follow-up skipped: complainant phone or consent missing", "NO_CONSENT");
        }

        String recipient = report.getRecipientPhone().trim();
        String body = formatFollowUpMessage(report);
        log.info("Sending follow-up reminder for {} to {}", report.getReportNumber(), recipient);
        return getActiveProvider().sendMessage(recipient, body, report.getReportNumber());
    }

    public MessageSendResult sendEscalationMessage(CivicReport report, String reason) {
        if (escalationRecipient == null || escalationRecipient.isBlank()) {
            log.warn("Escalation skipped for {}: CIVICEYE_ESCALATION_RECIPIENT is not configured", report.getReportNumber());
            return MessageSendResult.failure(getActiveProvider().getProviderName(),
                    "", "Escalation recipient is not configured", "NO_ESCALATION_RECIPIENT");
        }

        String body = formatEscalationMessage(report, reason);
        log.info("Dispatching escalation alert for {} to authority {}", report.getReportNumber(), escalationRecipient);
        return getActiveProvider().sendMessage(escalationRecipient.trim(), body, report.getReportNumber());
    }

    public boolean shouldNotifyComplainant(CivicReport report) {
        return report != null
                && report.isSmsConsent()
                && report.getRecipientPhone() != null
                && !report.getRecipientPhone().isBlank();
    }

    // =========================================================================
    // Message Body Formatters for CivicReport
    // =========================================================================

    public String formatSubmittedMessage(CivicReport report) {
        return "CivicEye: Report " + report.getReportNumber() + " (" + report.getCategory() + ") has been recorded. "
                + "Location: " + (report.getLocationText() != null ? report.getLocationText() : "Unspecified") + ". "
                + "Track status: http://localhost:5173/track?id=" + report.getReportNumber();
    }

    public String formatAssignedMessage(CivicReport report, String department) {
        return "CivicEye: Report " + report.getReportNumber() + " has been assigned to " + department + ". "
                + "Track progress: http://localhost:5173/track?id=" + report.getReportNumber();
    }

    public String formatStatusChangedMessage(CivicReport report, ReportStatus oldStatus, ReportStatus newStatus) {
        return "CivicEye: Report " + report.getReportNumber() + " status updated from "
                + oldStatus + " to " + newStatus + ". "
                + "Track: http://localhost:5173/track?id=" + report.getReportNumber();
    }

    public String formatFollowUpMessage(CivicReport report) {
        return "CivicEye Check-in: Has the " + report.getCategory() + " issue at "
                + (report.getLocationText() != null ? report.getLocationText() : "reported location")
                + " (Ref: " + report.getReportNumber() + ") been resolved? "
                + "Reply FIXED " + report.getReportNumber() + " or NOT_FIXED " + report.getReportNumber();
    }

    public String formatEscalationMessage(CivicReport report, String reason) {
        return "CivicEye ESCALATION: Report " + report.getReportNumber() + " (" + report.getCategory() + ") requires review. "
                + "Reason: " + reason + ". "
                + "Location: " + report.getLocationText();
    }

    // =========================================================================
    // Legacy Report Model Support (Maintained for Backward Compatibility)
    // =========================================================================

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
        String target = (escalationContact != null && !escalationContact.isBlank())
                ? escalationContact.trim()
                : escalationRecipient;

        if (target == null || target.isBlank()) {
            return MessageSendResult.failure(activeProvider.getProviderName(), escalationText,
                    "No escalation recipient configured", "NO_ESCALATION_RECIPIENT");
        }

        log.info("Sending escalation alert for Report {} via {} to {}", report.getId(), activeProvider.getProviderName(), target);
        return activeProvider.sendMessage(target, escalationText, report.getId());
    }

    public String formatComplaintMessage(Report report) {
        StringBuilder sb = new StringBuilder();
        sb.append("📢 [CivicEye Grievance Notice]\n");
        sb.append("Tracking ID: ").append(report.getId()).append("\n");
        sb.append("Summary: ").append(report.getTitle()).append(" (").append(report.getCategory()).append(")\n");
        sb.append("Location: ").append(report.getLocation() != null ? report.getLocation() : "Unspecified");

        if (report.getLatitude() != null && report.getLongitude() != null) {
            sb.append(" (https://maps.google.com/?q=").append(report.getLatitude()).append(",").append(report.getLongitude()).append(")");
        }
        sb.append("\n");

        if (report.getSuggestedDepartment() != null && !report.getSuggestedDepartment().isBlank()) {
            sb.append("Assigned Authority: ").append(report.getSuggestedDepartment()).append("\n");
        }

        sb.append("\nComplaint Details:\n\"").append(report.getComplaintText()).append("\"\n\n");
        sb.append("Reply Actions:\n");
        sb.append("• STATUS ").append(report.getId()).append(" : Check tracking status\n");
        sb.append("• FIXED ").append(report.getId()).append(" : Confirm issue is resolved\n");
        sb.append("• NOT_FIXED ").append(report.getId()).append(" : Escalate unresolved grievance\n\n");
        sb.append("Important: This sends an SMS notification; it does not automatically register an official government complaint. The department must agree to receive and process complaints through that channel.");
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

    public String getConfiguredProvider() {
        return configuredProvider;
    }

    public String getDefaultTestRecipient() {
        return defaultTestRecipient;
    }

    public String getEscalationRecipient() {
        return escalationRecipient;
    }
}
