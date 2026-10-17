package com.civiceye.repository;

import com.civiceye.entity.ReportStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportStatusHistoryRepository extends JpaRepository<ReportStatusHistory, UUID> {
    List<ReportStatusHistory> findByReportIdOrderByCreatedAtDesc(UUID reportId);
}
