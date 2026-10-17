package com.civiceye.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeStatusRequest(
        @NotBlank(message = "New status is required")
        String newStatus,
        String notes,
        String changedBy
) {}
