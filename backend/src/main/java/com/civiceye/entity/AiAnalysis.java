package com.civiceye.entity;

import com.civiceye.entity.converter.JsonListConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ai_analyses")
public class AiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    @JsonIgnore
    private CivicReport report;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "prompt_version", nullable = false, length = 50)
    private String promptVersion = "v1.0";

    @Convert(converter = JsonListConverter.class)
    @Column(name = "observations", nullable = false, columnDefinition = "TEXT")
    private List<String> observations = new ArrayList<>();

    @Column(name = "severity_reason", columnDefinition = "TEXT")
    private String severityReason;

    @Convert(converter = JsonListConverter.class)
    @Column(name = "uncertainties", columnDefinition = "TEXT")
    private List<String> uncertainties = new ArrayList<>();

    @Column(name = "suggested_department")
    private String suggestedDepartment;

    @Convert(converter = JsonListConverter.class)
    @Column(name = "next_steps", columnDefinition = "TEXT")
    private List<String> nextSteps = new ArrayList<>();

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AiAnalysis() {
    }

    public AiAnalysis(CivicReport report, String modelName, String promptVersion) {
        this.report = report;
        this.modelName = modelName;
        this.promptVersion = promptVersion;
        this.createdAt = Instant.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CivicReport getReport() {
        return report;
    }

    public void setReport(CivicReport report) {
        this.report = report;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public List<String> getObservations() {
        return observations;
    }

    public void setObservations(List<String> observations) {
        this.observations = observations;
    }

    public String getSeverityReason() {
        return severityReason;
    }

    public void setSeverityReason(String severityReason) {
        this.severityReason = severityReason;
    }

    public List<String> getUncertainties() {
        return uncertainties;
    }

    public void setUncertainties(List<String> uncertainties) {
        this.uncertainties = uncertainties;
    }

    public String getSuggestedDepartment() {
        return suggestedDepartment;
    }

    public void setSuggestedDepartment(String suggestedDepartment) {
        this.suggestedDepartment = suggestedDepartment;
    }

    public List<String> getNextSteps() {
        return nextSteps;
    }

    public void setNextSteps(List<String> nextSteps) {
        this.nextSteps = nextSteps;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
