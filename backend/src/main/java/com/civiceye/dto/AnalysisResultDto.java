package com.civiceye.dto;

import java.util.List;

public record AnalysisResultDto(
        String category,
        String title,
        List<String> observations,
        String severity,
        String severityReason,
        String suggestedDepartment,
        List<String> uncertainties,
        List<String> recommendedActions,
        String complaintText,
        boolean demoMode
) {}
