package com.civiceye.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateReportRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Category is required")
        String category,

        String description,
        String location,
        Double latitude,
        Double longitude,

        @NotBlank(message = "Complaint text is required")
        String complaintText,

        String severity,
        String severityReason,
        List<String> observations,
        String suggestedDepartment,
        List<String> uncertainties,
        List<String> recommendedActions,
        String status,
        String recipientPhone,
        boolean demoMode,
        String imageUrl
) {}
