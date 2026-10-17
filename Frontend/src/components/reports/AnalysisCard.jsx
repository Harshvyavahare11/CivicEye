import React, { useState } from 'react';
import StatusBadge from '../common/StatusBadge';
import Alert from '../common/Alert';

export function AnalysisCard({
  analysis,
  onSaveReport,
  isSaving,
  initialLocation,
  initialDescription,
  imagePreviewUrl,
  initialRecipientPhone = '+917058207967',
  latitude,
  longitude
}) {
  const [complaintText, setComplaintText] = useState(analysis.complaintText || '');
  const [title, setTitle] = useState(analysis.title || '');
  const [category, setCategory] = useState(analysis.category || 'Other');
  const [location, setLocation] = useState(initialLocation || '');
  const [recipientPhone, setRecipientPhone] = useState(initialRecipientPhone);
  const [showSendModal, setShowSendModal] = useState(false);

  const handleSave = (sendNow = false) => {
    onSaveReport({
      title,
      category,
      description: initialDescription,
      location,
      latitude,
      longitude,
      complaintText,
      severity: analysis.severity,
      severityReason: analysis.severityReason,
      observations: analysis.observations || [],
      suggestedDepartment: analysis.suggestedDepartment || '',
      uncertainties: analysis.uncertainties || [],
      recommendedActions: analysis.recommendedActions || [],
      status: sendNow ? 'Sent' : 'Ready for Submission',
      recipientPhone,
      demoMode: Boolean(analysis.demoMode),
      imageUrl: imagePreviewUrl,
      sendNow
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
            * AI-generated preliminary assessment based on visual evidence, requiring field verification.
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
            Recommended jurisdictional authority.
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
            Generated Complaint Draft (Review &amp; Edit Before Sending)
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

        <div className="form-group" style={{ marginTop: '1rem' }}>
          <label className="form-label" style={{ fontSize: '0.8125rem' }}>
            🏛️ Verified Government / Municipal Department Contact (SMS Dispatch)
          </label>
          <input
            type="text"
            className="form-input"
            value={recipientPhone}
            onChange={(e) => setRecipientPhone(e.target.value)}
            placeholder="+917058207967"
          />
          <div className="form-help" style={{ fontSize: '0.75rem', marginTop: '0.25rem', color: 'var(--text-secondary)' }}>
            To = verified government/municipal department number. From = authorized Twilio sender (+17372508034).
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
          <button
            type="button"
            className="btn btn-outline"
            onClick={() => handleSave(false)}
            disabled={isSaving || !complaintText.trim()}
          >
            {isSaving ? 'Saving...' : '💾 Save as Draft'}
          </button>

          <button
            type="button"
            className="btn btn-primary"
            onClick={() => setShowSendModal(true)}
            disabled={isSaving || !complaintText.trim()}
          >
            🚀 Send Complaint via Twilio SMS
          </button>
        </div>
      </div>

      {/* Confirmation Dialog Modal */}
      {showSendModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.75rem', color: 'var(--text-primary)' }}>
              A. Sending Complaint to Government Department
            </h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
              CivicEye will dispatch this grievance notice to the department via your authorized Twilio SMS sender.
            </p>

            <div style={{ backgroundColor: 'var(--bg-main)', padding: '0.875rem', borderRadius: 'var(--radius-sm)', marginBottom: '1rem', fontSize: '0.875rem' }}>
              <div><strong>To (Verified Department Contact):</strong> <code>{recipientPhone}</code></div>
              <div><strong>From (Authorized Twilio Sender):</strong> <code>+17372508034</code></div>
              <div><strong>Assigned Department:</strong> {analysis.suggestedDepartment || 'Public Works'}</div>
              <div><strong>Location:</strong> {location || 'Pinned Coordinates'}</div>
              <div style={{ marginTop: '0.5rem' }}>
                <strong>Body (Complaint Summary & Tracking ID):</strong>
                <blockquote style={{ fontStyle: 'italic', margin: '0.25rem 0 0', borderLeft: '3px solid var(--teal-500)', paddingLeft: '0.5rem', color: 'var(--text-secondary)' }}>
                  "{complaintText}"
                </blockquote>
              </div>
            </div>

            <div style={{ fontSize: '0.8rem', color: '#b45309', backgroundColor: '#fef3c7', padding: '0.75rem', borderRadius: '6px', marginBottom: '1.25rem', border: '1px solid #fde68a' }}>
              ⚠️ <strong>Important Notice:</strong> This sends an SMS; it does not automatically register an official government complaint. The department must agree to receive and process complaints through that channel.
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button
                type="button"
                className="btn btn-outline"
                onClick={() => setShowSendModal(false)}
                disabled={isSaving}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => {
                  setShowSendModal(false);
                  handleSave(true);
                }}
                disabled={isSaving}
              >
                {isSaving ? 'Dispatching...' : 'Yes, Dispatch Grievance'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AnalysisCard;
