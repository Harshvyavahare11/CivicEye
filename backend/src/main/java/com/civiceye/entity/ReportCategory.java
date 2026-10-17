package com.civiceye.entity;

public enum ReportCategory {
    POTHOLE,
    GARBAGE,
    OPEN_DRAIN,
    WATERLOGGING,
    OTHER;

    public static ReportCategory fromString(String value) {
        if (value == null || value.isBlank()) return OTHER;
        try {
            return ReportCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Check for friendly strings like "Open Drain" -> OPEN_DRAIN
            String normalized = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");
            for (ReportCategory cat : values()) {
                if (cat.name().equalsIgnoreCase(normalized)) {
                    return cat;
                }
            }
            return OTHER;
        }
    }
}
