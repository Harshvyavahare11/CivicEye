package com.civiceye.controller;

import com.civiceye.dto.AnalysisResultDto;
import com.civiceye.dto.CreateReportRequest;
import com.civiceye.dto.UpdateReportRequest;
import com.civiceye.model.Report;
import com.civiceye.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
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
     * Persists a citizen-confirmed civic report.
     */
    @PostMapping
    public ResponseEntity<Report> createReport(@Valid @RequestBody CreateReportRequest request) {
        Report created = reportService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Lists reports with optional category, status, and search query filters.
     */
    @GetMapping
    public ResponseEntity<List<Report>> listReports(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search
    ) {
        List<Report> reports = reportService.listReports(category, status, search);
        return ResponseEntity.ok(reports);
    }

    /**
     * Retrieves report details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable("id") String id) {
        return reportService.getReportById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing report (complaint text, location, description, or status).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Report> updateReport(
            @PathVariable("id") String id,
            @RequestBody UpdateReportRequest request
    ) {
        return reportService.updateReport(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a report by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable("id") String id) {
        boolean deleted = reportService.deleteReport(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Returns real statistics calculated from current reports.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(reportService.getStatistics());
    }
}
