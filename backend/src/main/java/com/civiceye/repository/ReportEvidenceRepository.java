package com.civiceye.repository;

import com.civiceye.entity.ReportEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportEvidenceRepository extends JpaRepository<ReportEvidence, UUID> {
    List<ReportEvidence> findByReportIdOrderByCreatedAtDesc(UUID reportId);
}
