package com.civiceye.dto;

import com.civiceye.entity.AiAnalysis;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AiAnalysisResponse(
        UUID id,
        UUID reportId,
        String modelName,
        String promptVersion,
        List<String> observations,
        String severityReason,
        List<String> uncertainties,
        String suggestedDepartment,
        List<String> nextSteps,
        Instant createdAt
) {
    public static AiAnalysisResponse fromEntity(AiAnalysis a) {
        if (a == null) return null;
        return new AiAnalysisResponse(
                a.getId(),
                a.getReport() != null ? a.getReport().getId() : null,
                a.getModelName(),
                a.getPromptVersion(),
                a.getObservations(),
                a.getSeverityReason(),
                a.getUncertainties(),
                a.getSuggestedDepartment(),
                a.getNextSteps(),
                a.getCreatedAt()
        );
    }
}
