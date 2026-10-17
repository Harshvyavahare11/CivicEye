package com.civiceye.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record SaveAnalysisRequest(
        @NotBlank(message = "Model name is required")
        String modelName,
        String promptVersion,
        List<String> observations,
        String severityReason,
        List<String> uncertainties,
        String suggestedDepartment,
        List<String> nextSteps,
        String rawResponse
) {}
