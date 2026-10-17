package com.civiceye.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Report model representing a civic grievance with full lifecycle tracking,
 * messaging delivery status, follow-up scheduling, and escalation history.
 */
public class Report {

    private String id;
    private String title;
    private String category;
    private String description;
    private String location;
    private Double latitude;
    private Double longitude;
    private String complaintText;
    private String severity;
    private String severityReason;
    private List<String> observations = new ArrayList<>();
    private String suggestedDepartment;
    private List<String> uncertainties = new ArrayList<>();
    private List<String> recommendedActions = new ArrayList<>();
    
    // Lifecycle and communication states (strictly separated)
    private String status; // Draft, Pending Send, Sent, Acknowledged, In Progress, Awaiting Citizen Confirmation, Resolved, Escalated, Closed
    private String communicationStatus; // Draft, Pending Send, Sent, Delivered, Failed
    private String recipientPhone;
    private String providerMessageId;
    
    private boolean demoMode;
    private String imageUrl;
    
    // Scheduling & Follow-ups
    private Instant nextFollowUpAt;
    private boolean followUpSent;
    private List<FollowUpRecord> followUpHistory = new ArrayList<>();
    
    // Inbound replies & Escalation
    private List<InboundMessageRecord> inboundMessages = new ArrayList<>();
    private List<EscalationRecord> escalationHistory = new ArrayList<>();
    private boolean citizenConfirmedResolved;

    private Instant createdAt;
    private Instant updatedAt;

    public Report() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.status = "Draft";
        this.communicationStatus = "Draft";
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

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

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

    public String getCommunicationStatus() { return communicationStatus; }
    public void setCommunicationStatus(String communicationStatus) { this.communicationStatus = communicationStatus; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getProviderMessageId() { return providerMessageId; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }

    public boolean isDemoMode() { return demoMode; }
    public void setDemoMode(boolean demoMode) { this.demoMode = demoMode; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Instant getNextFollowUpAt() { return nextFollowUpAt; }
    public void setNextFollowUpAt(Instant nextFollowUpAt) { this.nextFollowUpAt = nextFollowUpAt; }

    public boolean isFollowUpSent() { return followUpSent; }
    public void setFollowUpSent(boolean followUpSent) { this.followUpSent = followUpSent; }

    public List<FollowUpRecord> getFollowUpHistory() { return followUpHistory; }
    public void setFollowUpHistory(List<FollowUpRecord> followUpHistory) {
        this.followUpHistory = followUpHistory != null ? followUpHistory : new ArrayList<>();
    }

    public List<InboundMessageRecord> getInboundMessages() { return inboundMessages; }
    public void setInboundMessages(List<InboundMessageRecord> inboundMessages) {
        this.inboundMessages = inboundMessages != null ? inboundMessages : new ArrayList<>();
    }

    public List<EscalationRecord> getEscalationHistory() { return escalationHistory; }
    public void setEscalationHistory(List<EscalationRecord> escalationHistory) {
        this.escalationHistory = escalationHistory != null ? escalationHistory : new ArrayList<>();
    }

    public boolean isCitizenConfirmedResolved() { return citizenConfirmedResolved; }
    public void setCitizenConfirmedResolved(boolean citizenConfirmedResolved) { this.citizenConfirmedResolved = citizenConfirmedResolved; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
