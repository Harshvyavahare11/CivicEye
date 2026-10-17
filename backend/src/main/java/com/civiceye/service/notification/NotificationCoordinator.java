package com.civiceye.service.notification;

import com.civiceye.entity.CivicReport;
import com.civiceye.entity.NotificationLog;
import com.civiceye.entity.ReportStatus;
import com.civiceye.repository.NotificationLogRepository;
import com.civiceye.service.messaging.MessageSendResult;
import com.civiceye.service.messaging.MessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Coordinates notification lifecycle, idempotency checks, and audit logging.
 * Guarantees that notification failures never roll back core business transactions.
 */
@Service
public class NotificationCoordinator {

    private static final Logger log = LoggerFactory.getLogger(NotificationCoordinator.class);

    private final MessagingService messagingService;
    private final NotificationLogRepository logRepository;

    public NotificationCoordinator(MessagingService messagingService, NotificationLogRepository logRepository) {
        this.messagingService = messagingService;
        this.logRepository = logRepository;
    }

    /**
     * Dispatches submission confirmation if complainant consented and provided phone.
     */
    public void notifySubmission(CivicReport report) {
        if (!messagingService.shouldNotifyComplainant(report)) {
            log.debug("Submission notification skipped for {}: complainant phone or consent not provided", report.getReportNumber());
            return;
        }

        String idempotencyKey = report.getReportNumber() + ":SUBMISSION";
        if (isAlreadySent(idempotencyKey)) {
            log.info("Duplicate notification prevented for key: {}", idempotencyKey);
            return;
        }

        try {
            MessageSendResult result = messagingService.sendReportSubmitted(report);
            recordLog(report, "SUBMISSION", idempotencyKey, report.getRecipientPhone(), result);
        } catch (Exception ex) {
            log.error("Safe failure: notification failed for submission of {}: {}", report.getReportNumber(), ex.getMessage());
            recordFailure(report, "SUBMISSION", idempotencyKey, report.getRecipientPhone(), ex.getMessage(), "DISPATCH_EXCEPTION");
        }
    }

    /**
     * Dispatches department assignment notification.
     */
    public void notifyAssignment(CivicReport report, String department) {
        if (!messagingService.shouldNotifyComplainant(report) || department == null || department.isBlank()) {
            return;
        }

        String idempotencyKey = report.getReportNumber() + ":ASSIGNMENT:" + department.trim().toUpperCase().replace(" ", "_");
        if (isAlreadySent(idempotencyKey)) {
            log.info("Duplicate assignment notification prevented for key: {}", idempotencyKey);
            return;
        }

        try {
            MessageSendResult result = messagingService.sendReportAssigned(report, department);
            recordLog(report, "ASSIGNMENT", idempotencyKey, report.getRecipientPhone(), result);
        } catch (Exception ex) {
            log.error("Safe failure: assignment notification failed for {}: {}", report.getReportNumber(), ex.getMessage());
            recordFailure(report, "ASSIGNMENT", idempotencyKey, report.getRecipientPhone(), ex.getMessage(), "DISPATCH_EXCEPTION");
        }
    }

    /**
     * Dispatches notification upon status transition.
     */
    public void notifyStatusChange(CivicReport report, ReportStatus oldStatus, ReportStatus newStatus) {
        if (!messagingService.shouldNotifyComplainant(report)) {
            return;
        }

        String idempotencyKey = report.getReportNumber() + ":STATUS:" + newStatus.name();
        if (isAlreadySent(idempotencyKey)) {
            log.info("Duplicate status notification prevented for key: {}", idempotencyKey);
            return;
        }

        try {
            MessageSendResult result = messagingService.sendReportStatusChanged(report, oldStatus, newStatus);
            recordLog(report, "STATUS_CHANGE", idempotencyKey, report.getRecipientPhone(), result);
        } catch (Exception ex) {
            log.error("Safe failure: status change notification failed for {}: {}", report.getReportNumber(), ex.getMessage());
            recordFailure(report, "STATUS_CHANGE", idempotencyKey, report.getRecipientPhone(), ex.getMessage(), "DISPATCH_EXCEPTION");
        }
    }

    /**
     * Dispatches escalation alert to authority contact.
     */
    public void notifyEscalation(CivicReport report, String reason) {
        String escalationRecipient = messagingService.getEscalationRecipient();
        if (escalationRecipient == null || escalationRecipient.isBlank()) {
            log.warn("Escalation notification skipped for {}: CIVICEYE_ESCALATION_RECIPIENT not configured", report.getReportNumber());
            return;
        }

        String idempotencyKey = report.getReportNumber() + ":ESCALATION:" + System.currentTimeMillis();
        try {
            MessageSendResult result = messagingService.sendEscalationMessage(report, reason);
            recordLog(report, "ESCALATION", idempotencyKey, escalationRecipient, result);
        } catch (Exception ex) {
            log.error("Safe failure: escalation alert failed for {}: {}", report.getReportNumber(), ex.getMessage());
            recordFailure(report, "ESCALATION", idempotencyKey, escalationRecipient, ex.getMessage(), "DISPATCH_EXCEPTION");
        }
    }

    public boolean isAlreadySent(String idempotencyKey) {
        return logRepository.existsByIdempotencyKey(idempotencyKey);
    }

    public List<NotificationLog> getNotificationHistory(String reportNumber) {
        return logRepository.findByReportNumberOrderByCreatedAtDesc(reportNumber);
    }

    private void recordLog(CivicReport report, String eventType, String idempotencyKey, String recipient, MessageSendResult result) {
        String outcome = result.success()
                ? (result.isSimulated() ? "SIMULATED" : "ACCEPTED")
                : "FAILED";

        CivicReport persistedReport = (report != null && report.getId() != null) ? report : null;
        String reportNumber = report != null ? report.getReportNumber() : "UNKNOWN";

        NotificationLog logEntry = new NotificationLog(
                persistedReport,
                reportNumber,
                eventType,
                idempotencyKey,
                NotificationLog.maskPhoneNumber(recipient),
                result.providerName(),
                result.providerMessageId(),
                outcome,
                result.errorCode()
        );
        logRepository.save(logEntry);
    }

    private void recordFailure(CivicReport report, String eventType, String idempotencyKey, String recipient, String message, String errorCode) {
        CivicReport persistedReport = (report != null && report.getId() != null) ? report : null;
        String reportNumber = report != null ? report.getReportNumber() : "UNKNOWN";

        NotificationLog logEntry = new NotificationLog(
                persistedReport,
                reportNumber,
                eventType,
                idempotencyKey,
                NotificationLog.maskPhoneNumber(recipient),
                messagingService.getActiveProvider().getProviderName(),
                null,
                "FAILED",
                errorCode
        );
        logRepository.save(logEntry);
    }
}
