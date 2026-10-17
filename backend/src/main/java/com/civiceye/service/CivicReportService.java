package com.civiceye.service;

import com.civiceye.dto.*;
import com.civiceye.entity.*;
import com.civiceye.repository.*;
import com.civiceye.service.storage.StorageService;
import com.civiceye.service.storage.StorageService.StoredFile;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.Year;
import java.util.*;

@Service
public class CivicReportService {

    private static final Logger log = LoggerFactory.getLogger(CivicReportService.class);

    private final CivicReportRepository reportRepository;
    private final ReportEvidenceRepository evidenceRepository;
    private final AiAnalysisRepository analysisRepository;
    private final ReportStatusHistoryRepository statusHistoryRepository;
    private final StorageService storageService;

    public CivicReportService(
            CivicReportRepository reportRepository,
            ReportEvidenceRepository evidenceRepository,
            AiAnalysisRepository analysisRepository,
            ReportStatusHistoryRepository statusHistoryRepository,
            StorageService storageService
    ) {
        this.reportRepository = reportRepository;
        this.evidenceRepository = evidenceRepository;
        this.analysisRepository = analysisRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.storageService = storageService;
    }

    /**
     * Creates a new civic report. All new reports strictly begin in DRAFT status.
     */
    @Transactional
    public ReportResponse createDraftReport(CreateDraftReportRequest req) {
        String reportNumber = generateUniqueReportNumber();

        CivicReport report = new CivicReport();
        report.setReportNumber(reportNumber);
        report.setTitle(req.title().trim());
        report.setDescription(req.effectiveDescription());
        report.setCategory(ReportCategory.fromString(req.category()));
        report.setSeverity(ReportSeverity.fromString(req.severity()));
        report.setStatus(ReportStatus.DRAFT);
        report.setLocationText(req.effectiveLocation());
        report.setLatitude(req.latitude());
        report.setLongitude(req.longitude());
        report.setSuggestedDepartment(req.suggestedDepartment() != null ? req.suggestedDepartment().trim() : null);
        report.setRecipientPhone(req.recipientPhone() != null ? req.recipientPhone().trim() : null);
        report.setCommunicationStatus("Draft");

        CivicReport savedReport = reportRepository.save(report);

        // Record initial status history event
        ReportStatusHistory initialHistory = new ReportStatusHistory(
                savedReport,
                null,
                ReportStatus.DRAFT,
                "SYSTEM",
                "Initial civic report created in DRAFT state."
        );
        statusHistoryRepository.save(initialHistory);
        savedReport.getStatusHistory().add(initialHistory);

        log.info("Created civic report draft: ID={}, ReportNumber={}", savedReport.getId(), savedReport.getReportNumber());
        return ReportResponse.fromEntity(savedReport);
    }

    /**
     * Retrieves report details by UUID or human-readable report number (e.g. CE-2026-1001).
     */
    @Transactional(readOnly = true)
    public CivicReport findReportEntityByIdOrNumber(String idOrNumber) {
        if (idOrNumber == null || idOrNumber.isBlank()) {
            throw new NoSuchElementException("Report identifier is required.");
        }

        // Try UUID
        try {
            UUID uuid = UUID.fromString(idOrNumber.trim());
            Optional<CivicReport> byId = reportRepository.findById(uuid);
            if (byId.isPresent()) return byId.get();
        } catch (IllegalArgumentException ignored) {
            // Not a UUID, proceed to reportNumber lookup
        }

        // Lookup by human-readable reportNumber
        return reportRepository.findByReportNumber(idOrNumber.trim())
                .orElseThrow(() -> new NoSuchElementException("Report not found with identifier: " + idOrNumber));
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportDetails(String idOrNumber) {
        CivicReport report = findReportEntityByIdOrNumber(idOrNumber);
        return ReportResponse.fromEntity(report);
    }

    /**
     * Lists reports with filtering (category, status, search term, date range) and pagination.
     */
    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> listReports(
            ReportCategory category,
            ReportStatus status,
            String search,
            Instant createdAfter,
            Instant createdBefore,
            Pageable pageable
    ) {
        Specification<CivicReport> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate searchPred = cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("locationText")), pattern),
                        cb.like(cb.lower(root.get("reportNumber")), pattern)
                );
                predicates.add(searchPred);
            }
            if (createdAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
            }
            if (createdBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CivicReport> page = reportRepository.findAll(spec, pageable);
        return PageResponse.fromPage(page, ReportResponse::fromEntity);
    }

    /**
     * Edits report details. Permitted only while report is in DRAFT or READY_TO_SUBMIT status.
     */
    @Transactional
    public ReportResponse updateReportDetails(String idOrNumber, UpdateReportDetailsRequest req) {
        CivicReport report = findReportEntityByIdOrNumber(idOrNumber);

        if (report.getStatus() != ReportStatus.DRAFT && report.getStatus() != ReportStatus.READY_TO_SUBMIT) {
            throw new IllegalStateException("Report cannot be edited once it has transitioned to status: " + report.getStatus());
        }

        if (req.title() != null && !req.title().isBlank()) {
            report.setTitle(req.title().trim());
        }
        if (req.description() != null && !req.description().isBlank()) {
            report.setDescription(req.description().trim());
        }
        if (req.category() != null && !req.category().isBlank()) {
            report.setCategory(ReportCategory.fromString(req.category()));
        }
        if (req.locationText() != null) {
            report.setLocationText(req.locationText().trim());
        }
        if (req.latitude() != null) {
            report.setLatitude(req.latitude());
        }
        if (req.longitude() != null) {
            report.setLongitude(req.longitude());
        }

        CivicReport updated = reportRepository.save(report);
        log.info("Updated report details for: {}", updated.getReportNumber());
        return ReportResponse.fromEntity(updated);
    }

    /**
     * Requests a valid status transition. Validates state machine rules and records status history.
     */
    @Transactional
    public ReportResponse changeReportStatus(String idOrNumber, ChangeStatusRequest req) {
        CivicReport report = findReportEntityByIdOrNumber(idOrNumber);

        ReportStatus currentStatus = report.getStatus();
        ReportStatus targetStatus = ReportStatus.fromString(req.newStatus());

        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + currentStatus + " to " + targetStatus
            );
        }

        report.setStatus(targetStatus);

        if (targetStatus == ReportStatus.SUBMITTED && report.getSubmittedAt() == null) {
            report.setSubmittedAt(Instant.now());
        } else if (targetStatus == ReportStatus.RESOLVED && report.getResolvedAt() == null) {
            report.setResolvedAt(Instant.now());
        }

        CivicReport saved = reportRepository.save(report);

        // Record transition in status history
        ReportStatusHistory history = new ReportStatusHistory(
                saved,
                currentStatus,
                targetStatus,
                req.changedBy() != null ? req.changedBy().trim() : "USER",
                req.notes() != null ? req.notes().trim() : "Status updated to " + targetStatus
        );
        statusHistoryRepository.save(history);
        saved.getStatusHistory().add(0, history);

        log.info("Report {} status changed: {} -> {}", saved.getReportNumber(), currentStatus, targetStatus);
        return ReportResponse.fromEntity(saved);
    }

    /**
     * Appends an AI analysis to the report history without overwriting previous results.
     */
    @Transactional
    public AiAnalysisResponse saveAnalysis(String idOrNumber, SaveAnalysisRequest req) {
        CivicReport report = findReportEntityByIdOrNumber(idOrNumber);

        AiAnalysis analysis = new AiAnalysis(
                report,
                req.modelName().trim(),
                req.promptVersion() != null ? req.promptVersion().trim() : "v1.0"
        );
        analysis.setObservations(req.observations() != null ? req.observations() : Collections.emptyList());
        analysis.setSeverityReason(req.severityReason());
        analysis.setUncertainties(req.uncertainties() != null ? req.uncertainties() : Collections.emptyList());
        analysis.setSuggestedDepartment(req.suggestedDepartment());
        analysis.setNextSteps(req.nextSteps() != null ? req.nextSteps() : Collections.emptyList());
        analysis.setRawResponse(req.rawResponse());

        AiAnalysis savedAnalysis = analysisRepository.save(analysis);
        report.addAnalysis(savedAnalysis);

        if (req.suggestedDepartment() != null && !req.suggestedDepartment().isBlank()) {
            report.setSuggestedDepartment(req.suggestedDepartment().trim());
        }
        if (report.getStatus() == ReportStatus.DRAFT) {
            report.setStatus(ReportStatus.READY_TO_SUBMIT);
        }
        reportRepository.save(report);

        log.info("Saved AI analysis {} for report {}", savedAnalysis.getId(), report.getReportNumber());
        return AiAnalysisResponse.fromEntity(savedAnalysis);
    }

    /**
     * Stores photo metadata and links evidence to the report.
     */
    @Transactional
    public EvidenceResponse attachEvidence(String idOrNumber, MultipartFile file) {
        CivicReport report = findReportEntityByIdOrNumber(idOrNumber);

        StoredFile stored = storageService.storeEvidence(file);

        ReportEvidence evidence = new ReportEvidence(
                report,
                stored.originalFilename(),
                stored.contentType(),
                stored.sizeBytes(),
                stored.storageKey()
        );

        ReportEvidence saved = evidenceRepository.save(evidence);
        report.addEvidence(saved);
        reportRepository.save(report);

        log.info("Attached evidence {} to report {}", saved.getId(), report.getReportNumber());
        return EvidenceResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<AiAnalysisResponse> getAnalysisHistory(String idOrNumber) {
        CivicReport report = findReportEntityByIdOrNumber(idOrNumber);
        return analysisRepository.findByReportIdOrderByCreatedAtDesc(report.getId())
                .stream()
                .map(AiAnalysisResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> getStatusHistory(String idOrNumber) {
        CivicReport report = findReportEntityByIdOrNumber(idOrNumber);
        return statusHistoryRepository.findByReportIdOrderByCreatedAtDesc(report.getId())
                .stream()
                .map(StatusHistoryResponse::fromEntity)
                .toList();
    }

    /**
     * Returns real aggregate statistics calculated from current database rows.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        List<CivicReport> all = reportRepository.findAll();
        long total = all.size();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ReportStatus st : ReportStatus.values()) {
            long count = all.stream().filter(r -> r.getStatus() == st).count();
            byStatus.put(st.name(), count);
        }

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (ReportCategory cat : ReportCategory.values()) {
            long count = all.stream().filter(r -> r.getCategory() == cat).count();
            byCategory.put(cat.name(), count);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReports", total);
        stats.put("byStatus", byStatus);
        stats.put("byCategory", byCategory);
        return stats;
    }

    private synchronized String generateUniqueReportNumber() {
        int year = Year.now().getValue();
        long count = reportRepository.count() + 1;
        String candidate = String.format("CE-%d-%04d", year, count);
        while (reportRepository.existsByReportNumber(candidate)) {
            count++;
            candidate = String.format("CE-%d-%04d", year, count);
        }
        return candidate;
    }
}
