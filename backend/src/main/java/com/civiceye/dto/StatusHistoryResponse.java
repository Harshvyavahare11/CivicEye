package com.civiceye.dto;

import com.civiceye.entity.ReportStatusHistory;
import java.time.Instant;
import java.util.UUID;

public record StatusHistoryResponse(
        UUID id,
        UUID reportId,
        String previousStatus,
        String newStatus,
        String changedBy,
        String notes,
        Instant createdAt
) {
    public static StatusHistoryResponse fromEntity(ReportStatusHistory h) {
        if (h == null) return null;
        return new StatusHistoryResponse(
                h.getId(),
                h.getReport() != null ? h.getReport().getId() : null,
                h.getPreviousStatus() != null ? h.getPreviousStatus().name() : null,
                h.getNewStatus() != null ? h.getNewStatus().name() : null,
                h.getChangedBy(),
                h.getNotes(),
                h.getCreatedAt()
        );
    }
}
