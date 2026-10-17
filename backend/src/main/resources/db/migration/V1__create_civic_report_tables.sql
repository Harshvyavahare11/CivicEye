-- =============================================================================
-- CivicEye PostgreSQL Schema - V1__create_civic_report_tables.sql
-- =============================================================================

-- Ensure pgcrypto or native gen_random_uuid() is available
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Table 1: civic_reports
CREATE TABLE IF NOT EXISTS civic_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('POTHOLE', 'GARBAGE', 'OPEN_DRAIN', 'WATERLOGGING', 'OTHER')),
    severity VARCHAR(50) NOT NULL DEFAULT 'UNCERTAIN' CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'UNCERTAIN')),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'READY_TO_SUBMIT', 'SUBMITTED', 'ACKNOWLEDGED', 'IN_PROGRESS', 'RESOLVED', 'REJECTED', 'CLOSED')),
    location_text VARCHAR(500),
    latitude NUMERIC(10, 6),
    longitude NUMERIC(10, 6),
    suggested_department VARCHAR(255),
    recipient_phone VARCHAR(50),
    communication_status VARCHAR(50) DEFAULT 'Draft',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_civic_reports_report_number ON civic_reports(report_number);
CREATE INDEX IF NOT EXISTS idx_civic_reports_status ON civic_reports(status);
CREATE INDEX IF NOT EXISTS idx_civic_reports_category ON civic_reports(category);
CREATE INDEX IF NOT EXISTS idx_civic_reports_created_at ON civic_reports(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_civic_reports_location_text ON civic_reports(location_text);

-- Table 2: report_evidence
CREATE TABLE IF NOT EXISTS report_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES civic_reports(id) ON DELETE CASCADE,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_report_evidence_report_id ON report_evidence(report_id);

-- Table 3: ai_analyses
CREATE TABLE IF NOT EXISTS ai_analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES civic_reports(id) ON DELETE CASCADE,
    model_name VARCHAR(100) NOT NULL,
    prompt_version VARCHAR(50) NOT NULL DEFAULT 'v1.0',
    observations JSONB NOT NULL DEFAULT '[]'::jsonb,
    severity_reason TEXT,
    uncertainties JSONB DEFAULT '[]'::jsonb,
    suggested_department VARCHAR(255),
    next_steps JSONB DEFAULT '[]'::jsonb,
    raw_response JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ai_analyses_report_id ON ai_analyses(report_id);

-- Table 4: report_status_history
CREATE TABLE IF NOT EXISTS report_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES civic_reports(id) ON DELETE CASCADE,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_report_status_history_report_id ON report_status_history(report_id);

-- Optional Table: departments
CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    contact_information VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
