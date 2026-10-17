package com.civiceye.repository;

import com.civiceye.model.Report;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {

    Report save(Report report);

    Optional<Report> findById(String id);

    List<Report> findAll();

    List<Report> findByFilters(String category, String status, String search);

    boolean deleteById(String id);

    long count();
}
