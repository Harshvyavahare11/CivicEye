import React, { useState } from 'react';
import StatusBadge from '../common/StatusBadge';
import Alert from '../common/Alert';

export function AnalysisCard({
  analysis,
  onSaveReport,
  isSaving,
  initialLocation,
  initialDescription,
  imagePreviewUrl
}) {
  const [complaintText, setComplaintText] = useState(analysis.complaintText || '');
  const [title, setTitle] = useState(analysis.title || '');
  const [category, setCategory] = useState(analysis.category || 'Other');
  const [location, setLocation] = useState(initialLocation || '');

  const handleSave = () => {
    onSaveReport({
      title,
      category,
      description: initialDescription,
      location,
      complaintText,
      severity: analysis.severity,
      severityReason: analysis.severityReason,
      observations: analysis.observations || [],
      suggestedDepartment: analysis.suggestedDepartment || '',
      uncertainties: analysis.uncertainties || [],
      recommendedActions: analysis.recommendedActions || [],
      status: 'Ready for Submission',
      demoMode: Boolean(analysis.demoMode),
      imageUrl: imagePreviewUrl
    });
  };

  return (
    <div className="card" style={{ marginTop: '2rem', border: '2px solid var(--teal-500)' }}>
      {analysis.demoMode && (
        <Alert
          type="info"
          title="Demo Mode — Simulated AI Response"
          message="This assessment was generated using local deterministic simulation for demonstration purposes. Live Google Gemma 4 was not called."
        />
      )}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '1.25rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.8125rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--teal-700)' }}>
              AI Analysis Assessment
            </span>
            <StatusBadge status={analysis.severity} type="severity" />
          </div>
          <h2 style={{ fontSize: '1.5rem', color: 'var(--text-primary)' }}>
            {title}
          </h2>
        </div>

        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>Category</div>
          <span className="badge badge-demo" style={{ fontSize: '0.875rem' }}>{category}</span>
        </div>
      </div>

      {/* Severity Rationale & Department */}
      <div className="grid-2" style={{ marginBottom: '1.5rem', backgroundColor: 'var(--bg-main)', padding: '1rem', borderRadius: 'var(--radius-sm)' }}>
        <div>
          <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.25rem' }}>
            PRELIMINARY SEVERITY RATIONALE
          </div>
          <p style={{ fontSize: '0.9375rem', color: 'var(--text-secondary)' }}>
            {analysis.severityReason}
          </p>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
            * AI-generated preliminary assessment based on visual evidence, not an official municipal certification.
          </div>
        </div>

        <div>
          <div style={{ fontSize: '0.8125rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.25rem' }}>
            SUGGESTED MUNICIPAL DEPARTMENT
          </div>
          <div style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--teal-700)' }}>
            {analysis.suggestedDepartment || 'Public Works Department'}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.35rem' }}>
            Recommended authority based on issue jurisdiction.
          </div>
        </div>
      </div>

      {/* Concrete Observations */}
      {analysis.observations && analysis.observations.length > 0 && (
        <div style={{ marginBottom: '1.5rem' }}>
          <h3 style={{ fontSize: '1rem', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>
            Concrete Visual Observations
          </h3>
          <ul style={{ paddingLeft: '1.25rem', color: 'var(--text-secondary)', fontSize: '0.9375rem' }}>
            {analysis.observations.map((obs, idx) => (
              <li key={idx} style={{ marginBottom: '0.35rem' }}>{obs}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Uncertainties requiring verification */}
      {analysis.uncertainties && analysis.uncertainties.length > 0 && (
        <div style={{ marginBottom: '1.5rem', backgroundColor: '#fffbeb', padding: '1rem', borderRadius: 'var(--radius-sm)', border: '1px solid #fde68a' }}>
          <div style={{ fontSize: '0.875rem', fontWeight: 700, color: '#92400e', marginBottom: '0.35rem' }}>
            ⚠️ Factors Requiring On-Site Verification
          </div>
          <ul style={{ paddingLeft: '1.25rem', color: '#92400e', fontSize: '0.875rem' }}>
            {analysis.uncertainties.map((unc, idx) => (
              <li key={idx} style={{ marginBottom: '0.25rem' }}>{unc}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Recommended Actions */}
      {analysis.recommendedActions && analysis.recommendedActions.length > 0 && (
        <div style={{ marginBottom: '1.5rem' }}>
          <h3 style={{ fontSize: '1rem', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>
            Recommended Next Steps
          </h3>
          <ul style={{ paddingLeft: '1.25rem', color: 'var(--text-secondary)', fontSize: '0.9375rem' }}>
            {analysis.recommendedActions.map((act, idx) => (
              <li key={idx} style={{ marginBottom: '0.35rem' }}>{act}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Editable Complaint Section */}
      <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '1.5rem', marginTop: '1.5rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
          <label className="form-label" style={{ fontSize: '1rem', margin: 0 }}>
            Generated Complaint Draft (Review & Edit Before Saving)
          </label>
          <span style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>Editable by Citizen</span>
        </div>

        <textarea
          className="form-textarea"
          rows={4}
          value={complaintText}
          onChange={(e) => setComplaintText(e.target.value)}
          placeholder="Review and customize your grievance text..."
        />

        <div className="grid-2" style={{ marginTop: '1rem' }}>
          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label" style={{ fontSize: '0.8125rem' }}>Confirmed Location</label>
            <input
              type="text"
              className="form-input"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="e.g. Near Metro Pillar 12, Pune"
            />
          </div>

          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label" style={{ fontSize: '0.8125rem' }}>Report Title</label>
            <input
              type="text"
              className="form-input"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleSave}
            disabled={isSaving || !complaintText.trim()}
          >
            {isSaving ? 'Saving to CivicEye...' : '💾 Save Report to My Reports'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default AnalysisCard;
