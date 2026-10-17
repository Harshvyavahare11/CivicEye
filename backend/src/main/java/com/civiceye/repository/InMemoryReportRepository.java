package com.civiceye.repository;

import com.civiceye.model.Report;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory repository for development.
 * Holds reports in memory during the Spring Boot lifecycle.
 * Replaceable with Spring Data JPA repository in Phase 2.
 */
@Repository
public class InMemoryReportRepository implements ReportRepository {

    private final Map<String, Report> store = new ConcurrentHashMap<>();

    public InMemoryReportRepository() {
        seedInitialReports();
    }

    private void seedInitialReports() {
        Report report1 = new Report();
        report1.setId("CE-2026-1001");
        report1.setTitle("Severe pothole crater on MG Road");
        report1.setCategory("Pothole");
        report1.setDescription("Deep pothole causing sudden braking near metro pillar 42");
        report1.setLocation("MG Road near Pillar 42, Central Zone");
        report1.setComplaintText("A hazardous road crater has emerged on MG Road near Metro Pillar 42. Immediate asphalt patching is requested to prevent vehicle accidents.");
        report1.setSeverity("High");
        report1.setSeverityReason("Crater depth in fast vehicular lane presents imminent crash risk");
        report1.setSuggestedDepartment("Roads & Bridges Department");
        report1.setObservations(List.of("Cracked bituminous layer with exposed stone ballast", "Water accumulation inside the depression"));
        report1.setUncertainties(List.of("Actual depth measurement cannot be verified from 2D photograph alone"));
        report1.setRecommendedActions(List.of("Place temporary warning cones and deploy road patch crew"));
        report1.setStatus("Sent");
        report1.setCommunicationStatus("Sent");
        report1.setRecipientPhone("+15551234567");
        report1.setProviderMessageId("MOCK-MSG-INIT1");
        report1.setNextFollowUpAt(Instant.now().plus(2, ChronoUnit.MINUTES));
        report1.setDemoMode(true);
        report1.setCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS));
        report1.setUpdatedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        store.put(report1.getId(), report1);

        Report report2 = new Report();
        report2.setId("CE-2026-1002");
        report2.setTitle("Overflowing community garbage dump");
        report2.setCategory("Garbage");
        report2.setDescription("Community dumpster overflowing onto pedestrian sidewalk");
        report2.setLocation("Market Road, Sector 3");
        report2.setComplaintText("Excessive solid waste accumulation is blocking the pedestrian walkway on Market Road. Requesting urgent clearance and sanitation.");
        report2.setSeverity("Medium");
        report2.setSeverityReason("Hygienic concern and walkway obstruction");
        report2.setSuggestedDepartment("Solid Waste Management Department");
        report2.setObservations(List.of("Plastic bags and organic refuse overflowing around container", "Stray animal scavenging observed"));
        report2.setUncertainties(List.of("Duration of uncollected waste"));
        report2.setRecommendedActions(List.of("Dispatch waste compactor vehicle"));
        report2.setStatus("Ready for Submission");
        report2.setCommunicationStatus("Draft");
        report2.setRecipientPhone("+15551234567");
        report2.setDemoMode(true);
        report2.setCreatedAt(Instant.now().minus(6, ChronoUnit.HOURS));
        report2.setUpdatedAt(Instant.now().minus(6, ChronoUnit.HOURS));
        store.put(report2.getId(), report2);
    }

    @Override
    public Report save(Report report) {
        if (report.getId() == null || report.getId().isBlank()) {
            report.setId("CE-" + System.currentTimeMillis() % 1000000);
        }
        report.setUpdatedAt(Instant.now());
        store.put(report.getId(), report);
        return report;
    }

    @Override
    public Optional<Report> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Report> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Report::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Report> findByFilters(String category, String status, String search) {
        return store.values().stream()
                .filter(r -> category == null || category.isBlank() || category.equalsIgnoreCase("all") || r.getCategory().equalsIgnoreCase(category))
                .filter(r -> status == null || status.isBlank() || status.equalsIgnoreCase("all") || r.getStatus().equalsIgnoreCase(status))
                .filter(r -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.toLowerCase();
                    return (r.getTitle() != null && r.getTitle().toLowerCase().contains(q))
                            || (r.getDescription() != null && r.getDescription().toLowerCase().contains(q))
                            || (r.getLocation() != null && r.getLocation().toLowerCase().contains(q))
                            || (r.getId() != null && r.getId().toLowerCase().contains(q));
                })
                .sorted(Comparator.comparing(Report::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String id) {
        return store.remove(id) != null;
    }

    @Override
    public long count() {
        return store.size();
    }
}
