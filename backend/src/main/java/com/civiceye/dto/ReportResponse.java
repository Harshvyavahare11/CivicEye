package com.civiceye.dto;

import com.civiceye.entity.CivicReport;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        String reportNumber,
        String title,
        String description,
        String category,
        String severity,
        String status,
        String locationText,
        BigDecimal latitude,
        BigDecimal longitude,
        String suggestedDepartment,
        String recipientPhone,
        String communicationStatus,
        Instant createdAt,
        Instant updatedAt,
        Instant submittedAt,
        Instant resolvedAt,
        List<EvidenceResponse> evidence,
        AiAnalysisResponse latestAnalysis,
        int statusHistoryCount
) {
    public static ReportResponse fromEntity(CivicReport r) {
        if (r == null) return null;

        List<EvidenceResponse> evidenceResponses = r.getEvidence() != null
                ? r.getEvidence().stream().map(EvidenceResponse::fromEntity).toList()
                : Collections.emptyList();

        AiAnalysisResponse latest = (r.getAnalyses() != null && !r.getAnalyses().isEmpty())
                ? AiAnalysisResponse.fromEntity(r.getAnalyses().get(0))
                : null;

        int historyCount = r.getStatusHistory() != null ? r.getStatusHistory().size() : 0;

        return new ReportResponse(
                r.getId(),
                r.getReportNumber(),
                r.getTitle(),
                r.getDescription(),
                r.getCategory() != null ? r.getCategory().name() : null,
                r.getSeverity() != null ? r.getSeverity().name() : null,
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getLocationText(),
                r.getLatitude(),
                r.getLongitude(),
                r.getSuggestedDepartment(),
                r.getRecipientPhone(),
                r.getCommunicationStatus(),
                r.getCreatedAt(),
                r.getUpdatedAt(),
                r.getSubmittedAt(),
                r.getResolvedAt(),
                evidenceResponses,
                latest,
                historyCount
        );
    }
}
