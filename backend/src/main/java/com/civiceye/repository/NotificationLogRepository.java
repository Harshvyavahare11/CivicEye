package com.civiceye.repository;

import com.civiceye.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<NotificationLog> findByIdempotencyKey(String idempotencyKey);

    List<NotificationLog> findByReportIdOrderByCreatedAtDesc(UUID reportId);

    List<NotificationLog> findByReportNumberOrderByCreatedAtDesc(String reportNumber);
}
