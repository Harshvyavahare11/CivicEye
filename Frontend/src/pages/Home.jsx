import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getReportStats, getReports } from '../services/reportService';
import StatusBadge from '../components/common/StatusBadge';
import { formatDate } from '../utils/validators';

export function Home({ backendHealth, aiHealth }) {
  const [stats, setStats] = useState({
    totalReports: 0,
    byStatus: { 'Draft': 0, 'Ready for Submission': 0, 'Sent': 0, 'Resolved': 0, 'Escalated': 0 },
    byCommunication: { 'Draft': 0, 'Sent': 0, 'Delivered': 0, 'Failed': 0 }
  });
  const [recentReports, setRecentReports] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchDashboardData() {
      try {
        setLoading(true);
        const [statsData, reportsData] = await Promise.all([
          getReportStats().catch(() => null),
          getReports().catch(() => [])
        ]);

        if (statsData) {
          setStats(statsData);
        } else if (reportsData && reportsData.length) {
          setStats({
            totalReports: reportsData.length,
            byStatus: {
              'Draft': reportsData.filter(r => r.status === 'Draft').length,
              'Ready for Submission': reportsData.filter(r => r.status === 'Ready for Submission').length,
              'Sent': reportsData.filter(r => r.status === 'Sent').length,
              'Resolved': reportsData.filter(r => r.status === 'Resolved').length,
              'Escalated': reportsData.filter(r => r.status === 'Escalated').length
            },
            byCommunication: {
              'Draft': reportsData.filter(r => r.communicationStatus === 'Draft').length,
              'Sent': reportsData.filter(r => r.communicationStatus === 'Sent').length,
              'Delivered': reportsData.filter(r => r.communicationStatus === 'Delivered').length,
              'Failed': reportsData.filter(r => r.communicationStatus === 'Failed').length
            }
          });
        }

        if (Array.isArray(reportsData)) {
          setRecentReports(reportsData.slice(0, 5));
        }
      } finally {
        setLoading(false);
      }
    }

    fetchDashboardData();
  }, []);

  return (
    <div className="page-container container">
      {/* Hero / Overview Section */}
      <div style={{
        backgroundColor: 'var(--bg-surface)',
        border: '1px solid var(--border-subtle)',
        borderRadius: 'var(--radius-lg)',
        padding: '2.5rem',
        marginBottom: '2rem',
        boxShadow: 'var(--shadow-sm)',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '1.5rem'
      }}>
        <div style={{ maxWidth: '650px' }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.25rem 0.75rem',
            borderRadius: '9999px',
            backgroundColor: 'var(--teal-50)',
            color: 'var(--teal-700)',
            fontWeight: 600,
            fontSize: '0.8125rem',
            marginBottom: '0.75rem'
          }}>
            🏛️ AI-Powered Civic Grievance Management &amp; Follow-Up
          </div>
          <h1 style={{ fontSize: '2.25rem', marginBottom: '0.75rem', color: 'var(--text-primary)' }}>
            Empowering Citizens to Fix Local Infrastructure
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1.0625rem', lineHeight: 1.6 }}>
            CivicEye combines computer vision with Google Gemma 4 to analyze community problems—such as potholes, garbage accumulation, and broken streetlights—dispatching complaint notifications, scheduling automated follow-ups, and managing two-way escalations.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.875rem', flexWrap: 'wrap' }}>
          <Link
            to="/report"
            className="btn btn-primary"
            style={{ padding: '0.875rem 1.5rem', fontSize: '1rem', borderRadius: 'var(--radius-md)' }}
          >
            📸 Report an Issue
          </Link>
          <Link
            to="/track"
            className="btn btn-outline"
            style={{ padding: '0.875rem 1.5rem', fontSize: '1rem', borderRadius: 'var(--radius-md)', fontWeight: 600 }}
          >
            🔎 Track Issue
          </Link>
        </div>
      </div>

      {/* Real Statistics Calculated from Repository */}
      <div className="grid-4" style={{ marginBottom: '2.5rem' }}>
        <div className="card">
          <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            TOTAL CIVIC REPORTS
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--text-primary)', marginTop: '0.35rem' }}>
            {stats.totalReports}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Backed by live database records
          </div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            MESSAGES DISPATCHED
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--teal-700)', marginTop: '0.35rem' }}>
            {(stats.byCommunication?.['Sent'] || 0) + (stats.byCommunication?.['Delivered'] || 0)}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            SMS / WhatsApp notifications
          </div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            ESCALATED ISSUES
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: '#dc2626', marginTop: '0.35rem' }}>
            {stats.byStatus?.['Escalated'] || 0}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Higher authority notified
          </div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            RESOLVED ISSUES
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: '#047857', marginTop: '0.35rem' }}>
            {stats.byStatus?.['Resolved'] || 0}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Citizen verified resolutions
          </div>
        </div>
      </div>

      {/* How the CivicEye AI & Follow-up Flow Works */}
      <div className="card" style={{ marginBottom: '2.5rem', backgroundColor: 'var(--bg-surface)' }}>
        <h2 style={{ fontSize: '1.25rem', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>
          🔄 How the CivicEye AI &amp; Follow-Up Flow Works
        </h2>
        <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
          The end-to-end civic accountability loop connects citizens, artificial intelligence, and municipal operations:
        </p>

        <div className="flow-steps-grid">
          <div className="flow-step-card">
            <div className="step-num">1</div>
            <h4>Capture &amp; Locate</h4>
            <p>Upload a photo or snap with device camera. Pinpoint exact coordinates on the interactive OpenStreetMap.</p>
          </div>

          <div className="flow-step-card">
            <div className="step-num">2</div>
            <h4>Gemma 4 AI Analysis</h4>
            <p>Google Gemma 4 identifies the issue, evaluates preliminary severity, identifies responsible department, and drafts grievance.</p>
          </div>

          <div className="flow-step-card">
            <div className="step-num">3</div>
            <h4>Review &amp; Dispatch</h4>
            <p>Citizen reviews and edits complaint draft before sending via real WhatsApp or SMS messaging adapter.</p>
          </div>

          <div className="flow-step-card">
            <div className="step-num">4</div>
            <h4>Scheduled Follow-Up</h4>
            <p>Backend scheduler monitors open reports and automatically dispatches periodic status reminders.</p>
          </div>

          <div className="flow-step-card">
            <div className="step-num">5</div>
            <h4>Two-Way Resolution &amp; Escalation</h4>
            <p>Replying FIXED confirms resolution. Replying NOT_FIXED escalates the issue to higher authorities.</p>
          </div>
        </div>
      </div>

      {/* Service Connection Status Overview */}
      <div className="card" style={{ marginBottom: '2.5rem', backgroundColor: 'var(--bg-surface)' }}>
        <h3 style={{ fontSize: '1.125rem', marginBottom: '1rem', color: 'var(--text-primary)' }}>
          Service Architecture Health
        </h3>
        <div className="grid-2">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.75rem 1rem', backgroundColor: 'var(--bg-main)', borderRadius: 'var(--radius-sm)' }}>
            <div>
              <div style={{ fontWeight: 600, fontSize: '0.9375rem' }}>Spring Boot REST Backend (:8080)</div>
              <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                {backendHealth?.connected ? 'Running and responding to health check' : 'Disconnected / Unreachable'}
              </div>
            </div>
            <StatusBadge status={backendHealth?.status} type="connection" />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.75rem 1rem', backgroundColor: 'var(--bg-main)', borderRadius: 'var(--radius-sm)' }}>
            <div>
              <div style={{ fontWeight: 600, fontSize: '0.9375rem' }}>Python FastAPI AI Engine (:8000)</div>
              <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                {aiHealth?.connected
                  ? (aiHealth?.gemmaKeyConfigured ? `Google Gemma 4 (${aiHealth.model}) ready` : 'Demo simulation mode enabled')
                  : 'FastAPI offline or unreachable'}
              </div>
            </div>
            <span className={`badge ${aiHealth?.connected ? (aiHealth?.gemmaKeyConfigured ? 'badge-connected' : 'badge-demo') : 'badge-disconnected'}`}>
              {aiHealth?.connected ? (aiHealth?.gemmaKeyConfigured ? 'Gemma 4 Live' : 'Demo Available') : 'Disconnected'}
            </span>
          </div>
        </div>
      </div>

      {/* Recent Reports Table */}
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h2 style={{ fontSize: '1.25rem', color: 'var(--text-primary)' }}>
            Recent Civic Issue Reports
          </h2>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <Link to="/track" style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--teal-700)' }}>
              🔎 Track by ID →
            </Link>
            <Link to="/reports" style={{ fontSize: '0.875rem', fontWeight: 600 }}>
              View All ({stats.totalReports}) →
            </Link>
          </div>
        </div>

        {loading ? (
          <div className="card" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
            Loading civic reports...
          </div>
        ) : recentReports.length === 0 ? (
          <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
            <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>📋</div>
            <h3 style={{ fontSize: '1.125rem', marginBottom: '0.5rem' }}>No civic reports yet</h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9375rem', marginBottom: '1.25rem' }}>
              Be the first to report a local civic issue in your neighborhood.
            </p>
            <Link to="/report" className="btn btn-primary">
              Report First Issue
            </Link>
          </div>
        ) : (
          <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.875rem' }}>
                <thead>
                  <tr style={{ backgroundColor: 'var(--bg-main)', borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                    <th style={{ padding: '0.875rem 1.25rem' }}>ID &amp; Title</th>
                    <th style={{ padding: '0.875rem 1.25rem' }}>Category</th>
                    <th style={{ padding: '0.875rem 1.25rem' }}>Location</th>
                    <th style={{ padding: '0.875rem 1.25rem' }}>Lifecycle</th>
                    <th style={{ padding: '0.875rem 1.25rem' }}>Messaging</th>
                    <th style={{ padding: '0.875rem 1.25rem', textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {recentReports.map((report) => (
                    <tr key={report.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                      <td style={{ padding: '1rem 1.25rem' }}>
                        <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{report.title}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{report.id}</div>
                      </td>
                      <td style={{ padding: '1rem 1.25rem' }}>
                        <span className="badge badge-demo">{report.category}</span>
                      </td>
                      <td style={{ padding: '1rem 1.25rem', color: 'var(--text-secondary)' }}>
                        {report.location || 'Not specified'}
                      </td>
                      <td style={{ padding: '1rem 1.25rem' }}>
                        <StatusBadge status={report.status} />
                      </td>
                      <td style={{ padding: '1rem 1.25rem' }}>
                        <span className={`badge ${report.communicationStatus === 'Delivered' ? 'badge-success' : report.communicationStatus === 'Sent' ? 'badge-info' : 'badge-secondary'}`}>
                          {report.communicationStatus || 'Draft'}
                        </span>
                      </td>
                      <td style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                          <Link to={`/track/${report.id}`} className="btn btn-outline" style={{ padding: '0.35rem 0.65rem', fontSize: '0.8125rem' }}>
                            🔎 Track
                          </Link>
                          <Link to={`/reports/${report.id}`} className="btn btn-secondary" style={{ padding: '0.35rem 0.65rem', fontSize: '0.8125rem' }}>
                            Details
                          </Link>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Home;
