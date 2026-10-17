# CivicEye Database Schema Specification

This document details the report entity fields used by CivicEye and the planned relational schema for production deployment with **PostgreSQL** and **Spring Data JPA**.

---

## 1. Current V1 Development Storage Model

In V1, reports are held in a thread-safe in-memory concurrent repository (`InMemoryReportRepository`) using `ConcurrentHashMap<String, Report>`. The architecture is cleanly segregated behind the `ReportRepository` interface, allowing direct substitution with Spring Data JPA in production.

---

## 2. Planned Production PostgreSQL Schema

```sql
-- Enums
CREATE TYPE issue_severity AS ENUM (
    'Low',
    'Medium',
    'High',
    'Critical'
);

CREATE TYPE report_status AS ENUM (
    'Draft',
    'Ready for Submission',
    'Submitted',
    'Resolved'
);

-- Reports Table
CREATE TABLE reports (
    id VARCHAR(32) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(64) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    complaint_text TEXT NOT NULL,
    severity issue_severity NOT NULL DEFAULT 'Medium',
    severity_reason TEXT,
    suggested_department VARCHAR(128),
    observations JSONB DEFAULT '[]'::jsonb,
    uncertainties JSONB DEFAULT '[]'::jsonb,
    recommended_actions JSONB DEFAULT '[]'::jsonb,
    status report_status NOT NULL DEFAULT 'Draft',
    demo_mode BOOLEAN DEFAULT FALSE,
    image_url VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Query Optimization Indexes
CREATE INDEX idx_reports_category ON reports(category);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_created_at ON reports(created_at DESC);
CREATE INDEX idx_reports_search ON reports USING gin(to_tsvector('english', title || ' ' || description || ' ' || location));
```

---

## 3. Spring Data JPA Entity Mapping Example

```java
@Entity
@Table(name = "reports")
public class ReportEntity {

    @Id
    @Column(length = 32)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 64)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String complaintText;

    @Column(length = 32)
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String severityReason;

    private String suggestedDepartment;

    @Column(length = 32, nullable = false)
    private String status;

    private boolean demoMode;

    private String imageUrl;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
```
