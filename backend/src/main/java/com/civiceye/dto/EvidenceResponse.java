package com.civiceye.dto;

import com.civiceye.entity.ReportEvidence;
import java.time.Instant;
import java.util.UUID;

public record EvidenceResponse(
        UUID id,
        UUID reportId,
        String originalFilename,
        String contentType,
        Long fileSizeBytes,
        String storagePath,
        Instant createdAt
) {
    public static EvidenceResponse fromEntity(ReportEvidence e) {
        if (e == null) return null;
        return new EvidenceResponse(
                e.getId(),
                e.getReport() != null ? e.getReport().getId() : null,
                e.getOriginalFilename(),
                e.getContentType(),
                e.getFileSizeBytes(),
                e.getStoragePath(),
                e.getCreatedAt()
        );
    }
}
