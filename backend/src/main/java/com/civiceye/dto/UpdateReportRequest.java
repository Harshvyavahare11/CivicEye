package com.civiceye.dto;

public record UpdateReportRequest(
        String title,
        String category,
        String description,
        String location,
        String complaintText,
        String status
) {}
