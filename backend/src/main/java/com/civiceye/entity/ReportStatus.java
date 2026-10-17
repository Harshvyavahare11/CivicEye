package com.civiceye.entity;

import java.util.Set;

public enum ReportStatus {
    DRAFT,
    READY_TO_SUBMIT,
    SUBMITTED,
    ACKNOWLEDGED,
    IN_PROGRESS,
    RESOLVED,
    REJECTED,
    CLOSED;

    public boolean canTransitionTo(ReportStatus target) {
        if (target == null) return false;
        if (this == target) return true; // Idempotent

        return switch (this) {
            case DRAFT -> Set.of(READY_TO_SUBMIT, REJECTED, CLOSED).contains(target);
            case READY_TO_SUBMIT -> Set.of(DRAFT, SUBMITTED, REJECTED, CLOSED).contains(target);
            case SUBMITTED -> Set.of(ACKNOWLEDGED, IN_PROGRESS, REJECTED, CLOSED).contains(target);
            case ACKNOWLEDGED -> Set.of(IN_PROGRESS, REJECTED, CLOSED).contains(target);
            case IN_PROGRESS -> Set.of(RESOLVED, REJECTED, CLOSED).contains(target);
            case RESOLVED -> Set.of(CLOSED, IN_PROGRESS).contains(target);
            case REJECTED -> Set.of(DRAFT, CLOSED).contains(target);
            case CLOSED -> Set.of(DRAFT).contains(target);
        };
    }

    public static ReportStatus fromString(String value) {
        if (value == null || value.isBlank()) return DRAFT;
        String normalized = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        for (ReportStatus st : values()) {
            if (st.name().equalsIgnoreCase(normalized)) {
                return st;
            }
        }
        return DRAFT;
    }
}
