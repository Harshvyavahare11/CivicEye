package com.civiceye.dto;

import jakarta.validation.constraints.NotBlank;

public record SendComplaintRequest(
        @NotBlank(message = "Recipient phone is required")
        String recipientPhone,
        String customNote
) {}
