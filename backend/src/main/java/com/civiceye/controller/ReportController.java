package com.civiceye.controller;

import com.civiceye.dto.*;
import com.civiceye.entity.ReportCategory;
import com.civiceye.entity.ReportStatus;
import com.civiceye.service.CivicReportService;
import com.civiceye.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final CivicReportService civicReportService;
    private final ReportService reportService;

    public ReportController(CivicReportService civicReportService, ReportService reportService) {
        this.civicReportService = civicReportService;
        this.reportService = reportService;
    }

    /**
     * Analyzes an uploaded civic issue photograph by delegating to Python FastAPI (hosted Gemma 4).
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisResultDto> analyzeIssue(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "demoMode", required = false, defaultValue = "false") boolean demoMode
    ) {
        AnalysisResultDto result = reportService.analyzeIssue(image, description, location, demoMode);
        return ResponseEntity.ok(result);
    }

    /**
     * Creates a new civic report. All new reports are persisted strictly as DRAFT.
     */
    @PostMapping
    public ResponseEntity<ReportResponse> createDraftReport(@Valid @RequestBody CreateDraftReportRequest request) {
        ReportResponse created = civicReportService.createDraftReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieves report details by UUID or human-readable report number (e.g. CE-2026-1001).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReportDetails(@PathVariable("id") String id) {
        ReportResponse report = civicReportService.getReportDetails(id);
        return ResponseEntity.ok(report);
    }

    /**
     * Lists reports with pagination and optional filters for category, status, date, and search term.
     */
    @GetMapping
    public ResponseEntity<PageResponse<ReportResponse>> listReports(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "createdAfter", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAfter,
            @RequestParam(value = "createdBefore", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdBefore,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort
    ) {
        ReportCategory catEnum = (category != null && !category.equalsIgnoreCase("all"))
                ? ReportCategory.fromString(category)
                : null;
        ReportStatus statusEnum = (status != null && !status.equalsIgnoreCase("all"))
                ? ReportStatus.fromString(status)
                : null;

        String[] sortParts = sort.split(",");
        String sortProperty = sortParts[0];
        Sort.Direction direction = (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)), Sort.by(direction, sortProperty));

        PageResponse<ReportResponse> response = civicReportService.listReports(catEnum, statusEnum, search, createdAfter, createdBefore, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Edits report details while permitted by its status (DRAFT or READY_TO_SUBMIT).
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ReportResponse> updateReportDetails(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateReportDetailsRequest request
    ) {
        ReportResponse updated = civicReportService.updateReportDetails(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Requests a valid status transition for the report.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportResponse> changeReportStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody ChangeStatusRequest request
    ) {
        ReportResponse updated = civicReportService.changeReportStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Saves an AI analysis result linked to the report.
     */
    @PostMapping("/{id}/analyses")
    public ResponseEntity<AiAnalysisResponse> saveAnalysis(
            @PathVariable("id") String id,
            @Valid @RequestBody SaveAnalysisRequest request
    ) {
        AiAnalysisResponse analysis = civicReportService.saveAnalysis(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(analysis);
    }

    /**
     * Retrieves all historical AI analyses for a report.
     */
    @GetMapping("/{id}/analyses")
    public ResponseEntity<List<AiAnalysisResponse>> getAnalysisHistory(@PathVariable("id") String id) {
        List<AiAnalysisResponse> history = civicReportService.getAnalysisHistory(id);
        return ResponseEntity.ok(history);
    }

    /**
     * Retrieves the status transition audit history for a report.
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<StatusHistoryResponse>> getStatusHistory(@PathVariable("id") String id) {
        List<StatusHistoryResponse> history = civicReportService.getStatusHistory(id);
        return ResponseEntity.ok(history);
    }

    /**
     * Attaches an evidence photo to the report without storing binary blobs directly in PostgreSQL.
     */
    @PostMapping(value = "/{id}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceResponse> attachEvidence(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file
    ) {
        EvidenceResponse evidence = civicReportService.attachEvidence(id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(evidence);
    }

    /**
     * Sends the approved civic complaint to the configured recipient via WhatsApp or SMS.
     */
    @PostMapping("/{id}/send")
    public ResponseEntity<SendComplaintResponse> sendComplaint(
            @PathVariable("id") String id,
            @RequestBody(required = false) SendComplaintRequest request
    ) {
        String recipientPhone = (request != null) ? request.recipientPhone() : null;
        SendComplaintResponse response = reportService.sendComplaint(id, recipientPhone);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns real aggregate statistics calculated from database records.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(civicReportService.getStatistics());
    }
}
