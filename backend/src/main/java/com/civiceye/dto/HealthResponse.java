package com.civiceye.dto;

public record HealthResponse(
        String status,
        String service
) {
    public static HealthResponse up() {
        return new HealthResponse("UP", "CivicEye Backend");
    }
}
