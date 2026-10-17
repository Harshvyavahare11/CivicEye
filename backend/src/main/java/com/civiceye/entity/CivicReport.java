package com.civiceye.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "civic_reports")
public class CivicReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "report_number", nullable = false, unique = true, length = 50)
    private String reportNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ReportCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 50)
    private ReportSeverity severity = ReportSeverity.UNCERTAIN;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ReportStatus status = ReportStatus.DRAFT;

    @Column(name = "location_text", length = 500)
    private String locationText;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "suggested_department")
    private String suggestedDepartment;

    @Column(name = "recipient_phone", length = 50)
    private String recipientPhone;

    @Column(name = "sms_consent", nullable = false)
    private boolean smsConsent = false;

    @Column(name = "communication_status", length = 50)
    private String communicationStatus = "Draft";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<ReportEvidence> evidence = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<AiAnalysis> analyses = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private List<ReportStatusHistory> statusHistory = new ArrayList<>();

    public CivicReport() {
    }

    public CivicReport(String reportNumber, String title, String description, ReportCategory category) {
        this.reportNumber = reportNumber;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = ReportStatus.DRAFT;
        this.severity = ReportSeverity.UNCERTAIN;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    public void onPrePersist() {
        Instant now = Instant.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
        if (this.status == null) this.status = ReportStatus.DRAFT;
        if (this.severity == null) this.severity = ReportSeverity.UNCERTAIN;
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addEvidence(ReportEvidence ev) {
        this.evidence.add(ev);
        ev.setReport(this);
    }

    public void addAnalysis(AiAnalysis an) {
        this.analyses.add(an);
        an.setReport(this);
    }

    public void addStatusHistory(ReportStatusHistory sh) {
        this.statusHistory.add(sh);
        sh.setReport(this);
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(String reportNumber) {
        this.reportNumber = reportNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ReportCategory getCategory() {
        return category;
    }

    public void setCategory(ReportCategory category) {
        this.category = category;
    }

    public ReportSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ReportSeverity severity) {
        this.severity = severity;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getSuggestedDepartment() {
        return suggestedDepartment;
    }

    public void setSuggestedDepartment(String suggestedDepartment) {
        this.suggestedDepartment = suggestedDepartment;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public boolean isSmsConsent() {
        return smsConsent;
    }

    public void setSmsConsent(boolean smsConsent) {
        this.smsConsent = smsConsent;
    }

    public String getCommunicationStatus() {
        return communicationStatus;
    }

    public void setCommunicationStatus(String communicationStatus) {
        this.communicationStatus = communicationStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public List<ReportEvidence> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<ReportEvidence> evidence) {
        this.evidence = evidence;
    }

    public List<AiAnalysis> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(List<AiAnalysis> analyses) {
        this.analyses = analyses;
    }

    public List<ReportStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<ReportStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }
}
