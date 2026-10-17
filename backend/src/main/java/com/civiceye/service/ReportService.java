package com.civiceye.service;

import com.civiceye.dto.*;
import com.civiceye.exception.InvalidFileException;
import com.civiceye.model.Report;
import com.civiceye.repository.ReportRepository;
import com.civiceye.service.messaging.MessageSendResult;
import com.civiceye.service.messaging.MessagingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final ReportRepository reportRepository;
    private final AiClientService aiClientService;
    private final MessagingService messagingService;

    @Value("${civiceye.follow-up.delay-minutes:2}")
    private long followUpDelayMinutes;

    public ReportService(
            ReportRepository reportRepository,
            AiClientService aiClientService,
            MessagingService messagingService
    ) {
        this.reportRepository = reportRepository;
        this.aiClientService = aiClientService;
        this.messagingService = messagingService;
    }

    public AnalysisResultDto analyzeIssue(
            MultipartFile image,
            String description,
            String location,
            boolean demoMode
    ) {
        validateUploadedImage(image);

        try {
            return aiClientService.analyzeImage(image, description, location, demoMode);
        } catch (IOException ex) {
            log.error("Failed to read image bytes: {}", ex.getMessage());
            throw new InvalidFileException("Could not read uploaded image data: " + ex.getMessage(), 400);
        }
    }

    public Report createReport(CreateReportRequest req) {
        Report report = new Report();
        report.setId("CE-" + (System.currentTimeMillis() % 1000000));
        report.setTitle(req.title().trim());
        report.setCategory(req.category().trim());
        report.setDescription(req.description() != null ? req.description().trim() : "");
        report.setLocation(req.location() != null ? req.location().trim() : "");
        report.setLatitude(req.latitude());
        report.setLongitude(req.longitude());
        report.setComplaintText(req.complaintText().trim());
        report.setSeverity(req.severity() != null ? req.severity() : "Medium");
        report.setSeverityReason(req.severityReason() != null ? req.severityReason() : "");
        report.setObservations(req.observations());
        report.setSuggestedDepartment(req.suggestedDepartment() != null ? req.suggestedDepartment() : "");
        report.setUncertainties(req.uncertainties());
        report.setRecommendedActions(req.recommendedActions());
        report.setStatus(req.status() != null && !req.status().isBlank() ? req.status() : "Ready for Submission");
        report.setCommunicationStatus("Draft");
        report.setRecipientPhone(req.recipientPhone() != null && !req.recipientPhone().isBlank()
                ? req.recipientPhone().trim()
                : messagingService.getDefaultTestRecipient());
        report.setDemoMode(req.demoMode());
        report.setImageUrl(req.imageUrl());
        report.setCreatedAt(Instant.now());
        report.setUpdatedAt(Instant.now());

        log.info("Saving new report: ID={}, Title='{}', Status={}", report.getId(), report.getTitle(), report.getStatus());
        return reportRepository.save(report);
    }

    public SendComplaintResponse sendComplaint(String reportId, String recipientPhone) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new NoSuchElementException("Report " + reportId + " not found.");
        }

        Report report = reportOpt.get();
        String targetPhone = (recipientPhone != null && !recipientPhone.isBlank())
                ? recipientPhone.trim()
                : (report.getRecipientPhone() != null ? report.getRecipientPhone() : messagingService.getDefaultTestRecipient());

        report.setRecipientPhone(targetPhone);
        report.setCommunicationStatus("Pending Send");

        MessageSendResult result = messagingService.sendComplaintMessage(report, targetPhone);

        if (result.success()) {
            report.setProviderMessageId(result.providerMessageId());
            report.setCommunicationStatus(result.isDelivered() ? "Delivered" : "Sent");
            report.setStatus("Sent");
            // Schedule backend follow up
            report.setNextFollowUpAt(Instant.now().plus(Duration.ofMinutes(followUpDelayMinutes)));
            report.setFollowUpSent(false);
            report.setUpdatedAt(Instant.now());
            reportRepository.save(report);

            return new SendComplaintResponse(
                    report.getId(),
                    "SENT",
                    report.getCommunicationStatus(),
                    result.providerMessageId(),
                    targetPhone,
                    result.messageBody(),
                    result.providerName(),
                    result.isDelivered(),
                    result.isSimulated(),
                    Instant.now(),
                    result.isSimulated()
                            ? "Message simulated in development mode (no external SMS quota consumed)."
                            : "Message successfully accepted by " + result.providerName()
            );
        } else {
            report.setCommunicationStatus("Failed");
            report.setUpdatedAt(Instant.now());
            reportRepository.save(report);

            return new SendComplaintResponse(
                    report.getId(),
                    "FAILED",
                    "Failed",
                    null,
                    targetPhone,
                    result.messageBody(),
                    result.providerName(),
                    false,
                    result.isSimulated(),
                    Instant.now(),
                    "Delivery failed: " + result.errorMessage()
            );
        }
    }

    public List<Report> listReports(String category, String status, String search) {
        return reportRepository.findByFilters(category, status, search);
    }

    public Optional<Report> getReportById(String id) {
        return reportRepository.findById(id);
    }

    public Optional<Report> updateReport(String id, UpdateReportRequest req) {
        Optional<Report> existingOpt = reportRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        Report report = existingOpt.get();
        if (req.title() != null && !req.title().isBlank()) {
            report.setTitle(req.title().trim());
        }
        if (req.category() != null && !req.category().isBlank()) {
            report.setCategory(req.category().trim());
        }
        if (req.description() != null) {
            report.setDescription(req.description().trim());
        }
        if (req.location() != null) {
            report.setLocation(req.location().trim());
        }
        if (req.complaintText() != null && !req.complaintText().isBlank()) {
            report.setComplaintText(req.complaintText().trim());
        }
        if (req.status() != null && !req.status().isBlank()) {
            report.setStatus(req.status().trim());
        }
        report.setUpdatedAt(Instant.now());

        return Optional.of(reportRepository.save(report));
    }

    public boolean deleteReport(String id) {
        log.info("Deleting report ID={}", id);
        return reportRepository.deleteById(id);
    }

    public Map<String, Object> getStatistics() {
        List<Report> all = reportRepository.findAll();
        long total = all.size();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("Draft", all.stream().filter(r -> "Draft".equalsIgnoreCase(r.getStatus())).count());
        byStatus.put("Ready for Submission", all.stream().filter(r -> "Ready for Submission".equalsIgnoreCase(r.getStatus())).count());
        byStatus.put("Sent", all.stream().filter(r -> "Sent".equalsIgnoreCase(r.getStatus())).count());
        byStatus.put("Acknowledged", all.stream().filter(r -> "Acknowledged".equalsIgnoreCase(r.getStatus())).count());
        byStatus.put("In Progress", all.stream().filter(r -> "In Progress".equalsIgnoreCase(r.getStatus())).count());
        byStatus.put("Awaiting Citizen Confirmation", all.stream().filter(r -> "Awaiting Citizen Confirmation".equalsIgnoreCase(r.getStatus())).count());
        byStatus.put("Resolved", all.stream().filter(r -> "Resolved".equalsIgnoreCase(r.getStatus())).count());
        byStatus.put("Escalated", all.stream().filter(r -> "Escalated".equalsIgnoreCase(r.getStatus())).count());

        Map<String, Long> byCommunication = new LinkedHashMap<>();
        byCommunication.put("Draft", all.stream().filter(r -> "Draft".equalsIgnoreCase(r.getCommunicationStatus())).count());
        byCommunication.put("Sent", all.stream().filter(r -> "Sent".equalsIgnoreCase(r.getCommunicationStatus())).count());
        byCommunication.put("Delivered", all.stream().filter(r -> "Delivered".equalsIgnoreCase(r.getCommunicationStatus())).count());
        byCommunication.put("Failed", all.stream().filter(r -> "Failed".equalsIgnoreCase(r.getCommunicationStatus())).count());

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Report r : all) {
            String cat = r.getCategory() != null ? r.getCategory() : "Other";
            byCategory.put(cat, byCategory.getOrDefault(cat, 0L) + 1);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReports", total);
        stats.put("byStatus", byStatus);
        stats.put("byCommunication", byCommunication);
        stats.put("byCategory", byCategory);
        return stats;
    }

    private void validateUploadedImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new InvalidFileException("Image file is required and cannot be empty", 400);
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    "Only JPEG, PNG, and WebP images are allowed. Received: " + (contentType != null ? contentType : "unknown"),
                    415
            );
        }

        String originalFilename = image.getOriginalFilename();
        if (originalFilename != null) {
            String lower = originalFilename.toLowerCase();
            boolean validExtension = lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                    || lower.endsWith(".png") || lower.endsWith(".webp");
            if (!validExtension) {
                throw new InvalidFileException("Unsupported file extension. Allowed extensions: .jpg, .jpeg, .png, .webp", 415);
            }
        }
    }
}
