package com.civiceye.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateDraftReportRequest(
        @NotBlank(message = "Report title is required")
        String title,

        String description,
        String complaintText,
        String category,
        String severity,
        String locationText,
        String location,

        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
        BigDecimal longitude,

        String suggestedDepartment,
        String recipientPhone,
        Boolean smsConsent
) {
    public CreateDraftReportRequest(
            String title,
            String description,
            String complaintText,
            String category,
            String severity,
            String locationText,
            String location,
            BigDecimal latitude,
            BigDecimal longitude,
            String suggestedDepartment,
            String recipientPhone
    ) {
        this(title, description, complaintText, category, severity, locationText, location, latitude, longitude, suggestedDepartment, recipientPhone, false);
    }

    public boolean effectiveSmsConsent() {
        return Boolean.TRUE.equals(smsConsent);
    }

    public String effectiveDescription() {
        if (description != null && !description.isBlank()) return description.trim();
        if (complaintText != null && !complaintText.isBlank()) return complaintText.trim();
        return "Civic grievance draft";
    }

    public String effectiveLocation() {
        if (locationText != null && !locationText.isBlank()) return locationText.trim();
        if (location != null && !location.isBlank()) return location.trim();
        return null;
    }
}
