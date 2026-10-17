package com.civiceye.entity;

public enum ReportSeverity {
    LOW,
    MEDIUM,
    HIGH,
    UNCERTAIN;

    public static ReportSeverity fromString(String value) {
        if (value == null || value.isBlank()) return UNCERTAIN;
        try {
            return ReportSeverity.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNCERTAIN;
        }
    }
}
