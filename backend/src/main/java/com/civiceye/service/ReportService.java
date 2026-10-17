package com.civiceye.service;

import com.civiceye.dto.AnalysisResultDto;
import com.civiceye.dto.CreateReportRequest;
import com.civiceye.dto.UpdateReportRequest;
import com.civiceye.exception.InvalidFileException;
import com.civiceye.model.Report;
import com.civiceye.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    public ReportService(ReportRepository reportRepository, AiClientService aiClientService) {
        this.reportRepository = reportRepository;
        this.aiClientService = aiClientService;
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
        report.setComplaintText(req.complaintText().trim());
        report.setSeverity(req.severity() != null ? req.severity() : "Medium");
        report.setSeverityReason(req.severityReason() != null ? req.severityReason() : "");
        report.setObservations(req.observations());
        report.setSuggestedDepartment(req.suggestedDepartment() != null ? req.suggestedDepartment() : "");
        report.setUncertainties(req.uncertainties());
        report.setRecommendedActions(req.recommendedActions());
        report.setStatus(req.status() != null && !req.status().isBlank() ? req.status() : "Ready for Submission");
        report.setDemoMode(req.demoMode());
        report.setImageUrl(req.imageUrl());
        report.setCreatedAt(Instant.now());
        report.setUpdatedAt(Instant.now());

        log.info("Saving new report: ID={}, Title='{}', Status={}", report.getId(), report.getTitle(), report.getStatus());
        return reportRepository.save(report);
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
        byStatus.put("Submitted", all.stream().filter(r -> "Submitted".equalsIgnoreCase(r.getStatus())).count());
        byStatus.put("Resolved", all.stream().filter(r -> "Resolved".equalsIgnoreCase(r.getStatus())).count());

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Report r : all) {
            String cat = r.getCategory() != null ? r.getCategory() : "Other";
            byCategory.put(cat, byCategory.getOrDefault(cat, 0L) + 1);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReports", total);
        stats.put("byStatus", byStatus);
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
