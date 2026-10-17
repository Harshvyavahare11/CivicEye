package com.civiceye.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Report model representing a citizen-submitted civic issue grievance.
 */
public class Report {

    private String id;
    private String title;
    private String category;
    private String description;
    private String location;
    private String complaintText;
    private String severity;
    private String severityReason;
    private List<String> observations = new ArrayList<>();
    private String suggestedDepartment;
    private List<String> uncertainties = new ArrayList<>();
    private List<String> recommendedActions = new ArrayList<>();
    private String status;
    private boolean demoMode;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public Report() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.status = "Draft";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getComplaintText() { return complaintText; }
    public void setComplaintText(String complaintText) { this.complaintText = complaintText; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getSeverityReason() { return severityReason; }
    public void setSeverityReason(String severityReason) { this.severityReason = severityReason; }

    public List<String> getObservations() { return observations; }
    public void setObservations(List<String> observations) {
        this.observations = observations != null ? observations : new ArrayList<>();
    }

    public String getSuggestedDepartment() { return suggestedDepartment; }
    public void setSuggestedDepartment(String suggestedDepartment) { this.suggestedDepartment = suggestedDepartment; }

    public List<String> getUncertainties() { return uncertainties; }
    public void setUncertainties(List<String> uncertainties) {
        this.uncertainties = uncertainties != null ? uncertainties : new ArrayList<>();
    }

    public List<String> getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions != null ? recommendedActions : new ArrayList<>();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isDemoMode() { return demoMode; }
    public void setDemoMode(boolean demoMode) { this.demoMode = demoMode; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
