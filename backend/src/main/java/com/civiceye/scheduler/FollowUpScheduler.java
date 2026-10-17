package com.civiceye.scheduler;

import com.civiceye.model.FollowUpRecord;
import com.civiceye.model.Report;
import com.civiceye.repository.ReportRepository;
import com.civiceye.service.messaging.MessageSendResult;
import com.civiceye.service.messaging.MessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Backend scheduler managing automated follow-up messages for active civic complaints.
 */
@Component
public class FollowUpScheduler {

    private static final Logger log = LoggerFactory.getLogger(FollowUpScheduler.class);

    private final ReportRepository reportRepository;
    private final MessagingService messagingService;

    @Value("${civiceye.follow-up.enabled:true}")
    private boolean schedulerEnabled;

    public FollowUpScheduler(ReportRepository reportRepository, MessagingService messagingService) {
        this.reportRepository = reportRepository;
        this.messagingService = messagingService;
    }

    /**
     * Periodically inspects active reports and dispatches due follow-up checks.
     * Runs every 30 seconds to support both fast dev/demo intervals and real schedules.
     */
    @Scheduled(fixedDelayString = "${civiceye.scheduler.poll-interval-ms:30000}")
    public void processScheduledFollowUps() {
        if (!schedulerEnabled) return;

        Instant now = Instant.now();
        List<Report> allReports = reportRepository.findAll();

        for (Report report : allReports) {
            // Check eligibility:
            // 1. Must not be Resolved, Closed, or already Escalated
            if ("Resolved".equalsIgnoreCase(report.getStatus()) || "Closed".equalsIgnoreCase(report.getStatus()) || "Escalated".equalsIgnoreCase(report.getStatus())) {
                continue;
            }

            // 2. Must have nextFollowUpAt defined and due
            if (report.getNextFollowUpAt() == null || report.getNextFollowUpAt().isAfter(now)) {
                continue;
            }

            // 3. Must not have already sent follow-up for this cycle
            if (report.isFollowUpSent()) {
                continue;
            }

            // Report is eligible!
            dispatchFollowUp(report);
        }
    }

    public synchronized void dispatchFollowUp(Report report) {
        log.info("Processing due follow-up for report ID={}", report.getId());

        MessageSendResult sendResult = messagingService.sendFollowUpMessage(report);

        FollowUpRecord record = new FollowUpRecord(
                "FUP-" + UUID.randomUUID().toString().substring(0, 8),
                report.getNextFollowUpAt(),
                messagingService.formatFollowUpMessage(report)
        );
        record.setSentAt(Instant.now());
        record.setProviderMessageId(sendResult.providerMessageId());
        record.setStatus(sendResult.success() ? (sendResult.isDelivered() ? "DELIVERED" : "SENT") : "FAILED");

        report.getFollowUpHistory().add(record);
        report.setFollowUpSent(true);
        // Clear next follow up or schedule next stage if configured
        report.setNextFollowUpAt(null);
        report.setUpdatedAt(Instant.now());

        reportRepository.save(report);
        log.info("Follow-up successfully logged for report {}: Status={}", report.getId(), record.getStatus());
    }
}
