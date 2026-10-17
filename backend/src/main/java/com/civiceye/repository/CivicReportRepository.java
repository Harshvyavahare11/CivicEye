package com.civiceye.repository;

import com.civiceye.entity.CivicReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CivicReportRepository extends JpaRepository<CivicReport, UUID>, JpaSpecificationExecutor<CivicReport> {

    Optional<CivicReport> findByReportNumber(String reportNumber);

    boolean existsByReportNumber(String reportNumber);

    Page<CivicReport> findAll(Specification<CivicReport> spec, Pageable pageable);
}
