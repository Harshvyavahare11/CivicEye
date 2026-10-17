-- =============================================================================
-- CivicEye Migration: V2__create_notification_tables.sql
-- Adds SMS consent to civic_reports and creates notification audit & outbox table
-- =============================================================================

-- 1. Add sms_consent to civic_reports
ALTER TABLE civic_reports 
ADD COLUMN IF NOT EXISTS sms_consent BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Create notification_logs for delivery tracking and idempotency
CREATE TABLE IF NOT EXISTS notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID REFERENCES civic_reports(id) ON DELETE CASCADE,
    report_number VARCHAR(50),
    event_type VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(150) NOT NULL UNIQUE,
    recipient_masked VARCHAR(50) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_message_sid VARCHAR(100),
    outcome VARCHAR(30) NOT NULL CHECK (outcome IN ('PENDING', 'ACCEPTED', 'FAILED', 'SIMULATED')),
    error_code VARCHAR(100),
    attempt_count INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_attempted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_logs_report_id ON notification_logs(report_id);
CREATE INDEX IF NOT EXISTS idx_notification_logs_report_number ON notification_logs(report_number);
CREATE INDEX IF NOT EXISTS idx_notification_logs_outcome ON notification_logs(outcome);
