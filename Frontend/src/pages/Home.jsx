import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getReportStats, getReports } from '../services/reportService';
import StatusBadge from '../components/common/StatusBadge';
import { formatDate } from '../utils/validators';

export function Home({ backendHealth, aiHealth }) {
  const [stats, setStats] = useState({
    totalReports: 0,
    byStatus: { 'Draft': 0, 'Ready for Submission': 0, 'Submitted': 0, 'Resolved': 0 }
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
              'Submitted': reportsData.filter(r => r.status === 'Submitted').length,
              'Resolved': reportsData.filter(r => r.status === 'Resolved').length
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
            🏛️ AI-Powered Civic Grievance Management
          </div>
          <h1 style={{ fontSize: '2.25rem', marginBottom: '0.75rem', color: 'var(--text-primary)' }}>
            Empowering Citizens to Fix Local Infrastructure
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1.0625rem', lineHeight: 1.6 }}>
            CivicEye uses computer vision and Google Gemma 4 to analyze community problems—such as potholes, overflowing waste, and broken streetlights—generating structured, editable grievance drafts ready for municipal resolution.
          </p>
        </div>

        <div>
          <Link
            to="/report"
            className="btn btn-primary"
            style={{ padding: '0.875rem 1.75rem', fontSize: '1.0625rem', borderRadius: 'var(--radius-md)' }}
          >
            📸 Report an Issue Now
          </Link>
        </div>
      </div>

      {/* Real Statistics Calculated from Repository */}
      <div className="grid-4" style={{ marginBottom: '2.5rem' }}>
        <div className="card">
          <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            TOTAL LOCAL REPORTS
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--text-primary)', marginTop: '0.35rem' }}>
            {stats.totalReports}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Logged in current session
          </div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            READY FOR SUBMISSION
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: '#b45309', marginTop: '0.35rem' }}>
            {stats.byStatus?.['Ready for Submission'] || 0}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Analyzed & draft verified
          </div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            SUBMITTED GRIEVANCES
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--teal-700)', marginTop: '0.35rem' }}>
            {stats.byStatus?.['Submitted'] || 0}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Citizen marked submitted
          </div>
        </div>

        <div className="card">
          <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)' }}>
            RESOLVED
          </div>
          <div style={{ fontSize: '2rem', fontWeight: 800, color: '#047857', marginTop: '0.35rem' }}>
            {stats.byStatus?.['Resolved'] || 0}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
            Citizen marked resolved
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

      {/* Recent Reports Table/List */}
      <div>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h2 style={{ fontSize: '1.25rem', color: 'var(--text-primary)' }}>
            Recent Civic Issue Reports
          </h2>
          <Link to="/reports" style={{ fontSize: '0.875rem', fontWeight: 600 }}>
            View All Reports ({stats.totalReports}) →
          </Link>
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
                    <th style={{ padding: '0.875rem 1.25rem' }}>ID & Title</th>
                    <th style={{ padding: '0.875rem 1.25rem' }}>Category</th>
                    <th style={{ padding: '0.875rem 1.25rem' }}>Location</th>
                    <th style={{ padding: '0.875rem 1.25rem' }}>Severity</th>
                    <th style={{ padding: '0.875rem 1.25rem' }}>Status</th>
                    <th style={{ padding: '0.875rem 1.25rem' }}>Date</th>
                    <th style={{ padding: '0.875rem 1.25rem', textAlign: 'right' }}>Action</th>
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
                        <StatusBadge status={report.severity} type="severity" />
                      </td>
                      <td style={{ padding: '1rem 1.25rem' }}>
                        <StatusBadge status={report.status} />
                      </td>
                      <td style={{ padding: '1rem 1.25rem', color: 'var(--text-muted)', fontSize: '0.8125rem' }}>
                        {formatDate(report.createdAt)}
                      </td>
                      <td style={{ padding: '1rem 1.25rem', textAlign: 'right' }}>
                        <Link to={`/reports/${report.id}`} className="btn btn-secondary" style={{ padding: '0.35rem 0.75rem', fontSize: '0.8125rem' }}>
                          View Details
                        </Link>
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
