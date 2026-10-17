import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getReportById, updateReport, deleteReport } from '../services/reportService';
import StatusBadge from '../components/common/StatusBadge';
import Alert from '../components/common/Alert';
import { formatDate } from '../utils/validators';

export function ReportDetails() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Editable fields
  const [complaintText, setComplaintText] = useState('');
  const [location, setLocation] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState('Draft');
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    async function loadReport() {
      try {
        setLoading(true);
        const data = await getReportById(id);
        setReport(data);
        setComplaintText(data.complaintText || '');
        setLocation(data.location || '');
        setDescription(data.description || '');
        setStatus(data.status || 'Draft');
      } catch (err) {
        setError(`Failed to retrieve report ${id}: ${err.message}`);
      } finally {
        setLoading(false);
      }
    }

    loadReport();
  }, [id]);

  const handleSave = async () => {
    setIsSaving(true);
    setSuccessMsg(null);
    setError(null);
    try {
      const updated = await updateReport(id, {
        complaintText,
        location,
        description,
        status
      });
      setReport(updated);
      setSuccessMsg('Report updated successfully!');
      setTimeout(() => setSuccessMsg(null), 3500);
    } catch (err) {
      setError(`Failed to save updates: ${err.message}`);
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm(`Are you sure you want to delete report ${id}?`)) return;
    try {
      await deleteReport(id);
      navigate('/reports');
    } catch (err) {
      setError(`Failed to delete: ${err.message}`);
    }
  };

  if (loading) {
    return (
      <div className="page-container container" style={{ textAlign: 'center', padding: '4rem' }}>
        <p style={{ color: 'var(--text-muted)' }}>Loading report details...</p>
      </div>
    );
  }

  if (error && !report) {
    return (
      <div className="page-container container">
        <Alert type="error" title="Report Not Found" message={error} />
        <Link to="/reports" className="btn btn-secondary">← Back to My Reports</Link>
      </div>
    );
  }

  return (
    <div className="page-container container" style={{ maxWidth: '900px' }}>
      <div style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Link to="/reports" style={{ fontWeight: 600, fontSize: '0.875rem' }}>
          ← Back to Reports List
        </Link>
        <button type="button" className="btn btn-danger" onClick={handleDelete} style={{ padding: '0.4rem 0.85rem', fontSize: '0.8125rem' }}>
          Delete Report
        </button>
      </div>

      {successMsg && <Alert type="success" message={successMsg} />}
      {error && <Alert type="error" message={error} onDismiss={() => setError(null)} />}

      <div className="card" style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '1.25rem', marginBottom: '1.5rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.875rem', fontWeight: 700, color: 'var(--teal-700)' }}>
                {report.id}
              </span>
              <StatusBadge status={report.severity} type="severity" />
              <StatusBadge status={report.status} />
              {report.demoMode && <span className="badge badge-demo">Demo Simulated</span>}
            </div>
            <h1 style={{ fontSize: '1.75rem', color: 'var(--text-primary)' }}>
              {report.title}
            </h1>
          </div>

          <div style={{ textAlign: 'right', fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
            <div>Created: {formatDate(report.createdAt)}</div>
            <div>Updated: {formatDate(report.updatedAt)}</div>
          </div>
        </div>

        {/* Image Preview if available */}
        {report.imageUrl && (
          <div className="preview-container" style={{ marginBottom: '1.5rem', maxHeight: '350px' }}>
            <img src={report.imageUrl} alt="Uploaded grievance" className="preview-image" />
          </div>
        )}

        {/* AI Analysis Summary */}
        <div style={{ backgroundColor: 'var(--bg-main)', padding: '1.25rem', borderRadius: 'var(--radius-sm)', marginBottom: '1.5rem' }}>
          <h3 style={{ fontSize: '1rem', marginBottom: '0.5rem', color: 'var(--teal-700)' }}>
            Preliminary Assessment & Jurisdiction
          </h3>
          <div className="grid-2">
            <div>
              <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)' }}>SEVERITY RATIONALE</div>
              <p style={{ fontSize: '0.9375rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                {report.severityReason || 'Visual defect detected on civic property.'}
              </p>
            </div>
            <div>
              <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)' }}>RESPONSIBLE DEPARTMENT</div>
              <div style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: '0.25rem' }}>
                {report.suggestedDepartment || 'Municipal Engineering Dept'}
              </div>
            </div>
          </div>
        </div>

        {/* Concrete Observations */}
        {report.observations && report.observations.length > 0 && (
          <div style={{ marginBottom: '1.5rem' }}>
            <h4 style={{ fontSize: '0.9375rem', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>
              Visual Observations
            </h4>
            <ul style={{ paddingLeft: '1.25rem', color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
              {report.observations.map((obs, idx) => (
                <li key={idx} style={{ marginBottom: '0.25rem' }}>{obs}</li>
              ))}
            </ul>
          </div>
        )}

        {/* Uncertainties */}
        {report.uncertainties && report.uncertainties.length > 0 && (
          <div style={{ marginBottom: '1.5rem', backgroundColor: '#fffbeb', padding: '0.875rem 1rem', borderRadius: 'var(--radius-sm)', border: '1px solid #fde68a' }}>
            <div style={{ fontSize: '0.8125rem', fontWeight: 700, color: '#92400e', marginBottom: '0.25rem' }}>
              ⚠️ Verification Factors
            </div>
            <ul style={{ paddingLeft: '1.25rem', color: '#92400e', fontSize: '0.8125rem' }}>
              {report.uncertainties.map((unc, idx) => (
                <li key={idx}>{unc}</li>
              ))}
            </ul>
          </div>
        )}

        {/* Editable Citizen Complaint & Status */}
        <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '1.5rem' }}>
          <h3 style={{ fontSize: '1.125rem', marginBottom: '1rem', color: 'var(--text-primary)' }}>
            Editable Grievance Details & Status
          </h3>

          <div className="form-group">
            <label className="form-label">Complaint Text</label>
            <textarea
              className="form-textarea"
              rows={4}
              value={complaintText}
              onChange={(e) => setComplaintText(e.target.value)}
            />
          </div>

          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Confirmed Location</label>
              <input
                type="text"
                className="form-input"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Report Lifecycle Status</label>
              <select
                className="form-select"
                value={status}
                onChange={(e) => setStatus(e.target.value)}
              >
                <option value="Draft">Draft</option>
                <option value="Ready for Submission">Ready for Submission</option>
                <option value="Submitted">Submitted (Citizen Marked)</option>
                <option value="Resolved">Resolved</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Citizen Notes / Description</label>
            <textarea
              className="form-textarea"
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleSave}
              disabled={isSaving}
            >
              {isSaving ? 'Saving Changes...' : '💾 Save Changes'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ReportDetails;
