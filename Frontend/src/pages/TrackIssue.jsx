import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getReportById, simulateWebhook } from '../services/reportService';
import LocationMap from '../components/common/LocationMap';

export default function TrackIssue() {
  const { id: paramId } = useParams();
  const navigate = useNavigate();

  const [searchId, setSearchId] = useState(paramId || '');
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Simulator state
  const [simBody, setSimBody] = useState('');
  const [simFrom, setSimFrom] = useState('+15551234567');
  const [simLoading, setSimLoading] = useState(false);
  const [simResult, setSimResult] = useState(null);

  useEffect(() => {
    if (paramId) {
      setSearchId(paramId);
      loadReport(paramId);
    }
  }, [paramId]);

  const loadReport = async (reportId) => {
    if (!reportId || !reportId.trim()) return;
    setLoading(true);
    setError(null);
    setSimResult(null);

    try {
      const data = await getReportById(reportId.trim());
      setReport(data);
      if (data && data.recipientPhone) {
        setSimFrom(data.recipientPhone);
      }
    } catch (err) {
      setError(err.message || 'Report not found. Please verify the Report ID.');
      setReport(null);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchId.trim()) {
      navigate(`/track/${searchId.trim()}`);
      loadReport(searchId.trim());
    }
  };

  const handleSimulateReply = async (keyword) => {
    if (!report) return;
    const bodyText = keyword ? `${keyword} ${report.id}` : simBody;
    if (!bodyText.trim()) return;

    setSimLoading(true);
    setSimResult(null);

    try {
      const res = await simulateWebhook(simFrom, bodyText.trim(), report.id);
      setSimResult(res);
      // Refresh report data to reflect incoming webhook changes
      await loadReport(report.id);
    } catch (err) {
      setSimResult({
        success: false,
        replyText: 'Simulation error: ' + err.message
      });
    } finally {
      setSimLoading(false);
    }
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'Resolved': return 'badge-success';
      case 'Awaiting Citizen Confirmation': return 'badge-info';
      case 'Escalated': return 'badge-danger';
      case 'In Progress': return 'badge-warning';
      case 'Sent': return 'badge-primary';
      case 'Acknowledged': return 'badge-primary';
      default: return 'badge-secondary';
    }
  };

  const getCommBadgeClass = (commStatus) => {
    switch (commStatus) {
      case 'Delivered': return 'badge-success';
      case 'Sent': return 'badge-info';
      case 'Pending Send': return 'badge-warning';
      case 'Failed': return 'badge-danger';
      default: return 'badge-secondary';
    }
  };

  return (
    <div className="track-page-container">
      {/* Header & Search Bar */}
      <div className="track-header-banner">
        <h1 className="track-main-title">🔎 Track Civic Issue &amp; Two-Way Grievance</h1>
        <p className="track-subtitle">
          Real-time tracking of report status, messaging delivery, scheduled follow-up countdowns, and two-way citizen replies.
        </p>

        <form onSubmit={handleSearchSubmit} className="track-search-box">
          <input
            type="text"
            className="track-search-input"
            placeholder="Enter Report ID (e.g. CE-2026-1001, CE-2026-1002)..."
            value={searchId}
            onChange={(e) => setSearchId(e.target.value)}
          />
          <button type="submit" className="btn btn-primary track-search-btn" disabled={loading}>
            {loading ? 'Searching...' : 'Track Report'}
          </button>
        </form>
      </div>

      {error && (
        <div className="alert alert-danger" style={{ marginTop: '1.5rem' }}>
          <strong>Error:</strong> {error}
        </div>
      )}

      {/* Report Details View */}
      {report && (
        <div className="track-report-card">
          <div className="track-top-bar">
            <div>
              <span className="track-id-badge">{report.id}</span>
              <h2 className="track-title">{report.title}</h2>
              <span className="track-category-tag">{report.category}</span>
            </div>
            <div className="track-badges-stack">
              <div className="track-badge-group">
                <span className="track-badge-label">Issue Lifecycle:</span>
                <span className={`badge ${getStatusBadgeClass(report.status)}`}>
                  {report.status}
                </span>
              </div>
              <div className="track-badge-group">
                <span className="track-badge-label">Messaging Status:</span>
                <span className={`badge ${getCommBadgeClass(report.communicationStatus || 'Draft')}`}>
                  {report.communicationStatus || 'Draft'}
                </span>
              </div>
            </div>
          </div>

          {/* Lifecycle vs Communication Disclaimer */}
          <div className="lifecycle-notice-box">
            <span className="notice-icon">ℹ️</span>
            <span>
              <strong>CivicEye Verification Policy:</strong> Messaging delivery confirms dispatch to provider. Official acknowledgment and resolution require government confirmation or citizen verification.
            </span>
          </div>

          <div className="track-grid">
            {/* Left Column: Complaint & Meta */}
            <div className="track-left-col">
              <div className="track-section">
                <h3>📝 Citizen Description</h3>
                <p className="track-desc-text">
                  {report.description || 'No supplementary citizen description provided.'}
                </p>
              </div>

              <div className="track-section">
                <h3>🏛️ Official Complaint Text Dispatched</h3>
                <blockquote className="track-complaint-quote">
                  "{report.complaintText}"
                </blockquote>
                <div className="track-meta-row">
                  <div><strong>Assigned Authority:</strong> {report.suggestedDepartment || 'Municipal Operations'}</div>
                  <div><strong>Assessed Severity:</strong> <span className={`severity-tag severity-${report.severity?.toLowerCase()}`}>{report.severity}</span></div>
                  <div><strong>Severity Basis:</strong> {report.severityReason || 'Standard civic defect assessment'}</div>
                </div>
              </div>

              {/* Messaging Delivery Record */}
              <div className="track-section">
                <h3>📡 Communication Details</h3>
                <div className="comm-detail-grid">
                  <div><strong>Recipient Contact:</strong> {report.recipientPhone || 'Unconfigured'}</div>
                  <div><strong>Provider Message ID:</strong> <code>{report.providerMessageId || 'N/A (Draft / Local)'}</code></div>
                  <div><strong>Delivery Status:</strong> {report.communicationStatus || 'Draft'}</div>
                  <div><strong>Created:</strong> {new Date(report.createdAt).toLocaleString()}</div>
                  <div><strong>Last Updated:</strong> {new Date(report.updatedAt).toLocaleString()}</div>
                </div>
              </div>

              {/* Follow-Up Schedule */}
              <div className="track-section follow-up-section">
                <h3>⏰ Automated Backend Follow-Up</h3>
                {report.nextFollowUpAt ? (
                  <div className="follow-up-card">
                    <p>
                      <strong>Next Scheduled Follow-Up:</strong>{' '}
                      <span className="follow-up-time">{new Date(report.nextFollowUpAt).toLocaleString()}</span>
                    </p>
                    <p className="follow-up-status">
                      Status: {report.followUpSent ? 'Follow-up message dispatched' : 'Pending automated dispatch by backend scheduler'}
                    </p>
                  </div>
                ) : (
                  <p className="text-muted">No pending automated follow-up scheduled for this stage.</p>
                )}

                {report.followUpHistory && report.followUpHistory.length > 0 && (
                  <div className="history-list">
                    <h4>Follow-Up Dispatch Logs:</h4>
                    <ul>
                      {report.followUpHistory.map((f, idx) => (
                        <li key={idx}>
                          <span>[{new Date(f.sentAt).toLocaleTimeString()}]</span> Stage {f.stage}: {f.status} (Target: {f.recipient})
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>

              {/* Escalation History */}
              {report.escalationHistory && report.escalationHistory.length > 0 && (
                <div className="track-section escalation-section">
                  <h3>🚨 Escalation Records</h3>
                  <div className="escalation-alert-card">
                    {report.escalationHistory.map((esc, idx) => (
                      <div key={idx} className="escalation-item">
                        <div className="esc-header">
                          <span className="badge badge-danger">ESCALATED</span>
                          <span className="esc-time">{new Date(esc.escalatedAt).toLocaleString()}</span>
                        </div>
                        <p><strong>Reason:</strong> {esc.reason}</p>
                        <p><strong>Notified Authority:</strong> {esc.targetAuthority} (Status: {esc.status})</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Right Column: Location & Live Reply Simulator */}
            <div className="track-right-col">
              <div className="track-section">
                <h3>📍 Confirmed Civic Location</h3>
                <LocationMap
                  latitude={report.latitude || 18.5204}
                  longitude={report.longitude || 73.8567}
                  address={report.location}
                  readOnly={true}
                  height="220px"
                />
              </div>

              {/* Two-Way SMS / WhatsApp Simulator */}
              <div className="track-section simulator-section">
                <div className="sim-header">
                  <h3>💬 Two-Way WhatsApp / SMS Simulator</h3>
                  <span className="sim-badge">Interactive Test Tool</span>
                </div>
                <p className="sim-instruction">
                  Simulate incoming citizen or authority replies to test the live webhook and status state transitions:
                </p>

                <div className="sim-quick-buttons">
                  <button
                    type="button"
                    className="btn btn-outline btn-sm"
                    onClick={() => handleSimulateReply('STATUS')}
                    disabled={simLoading}
                  >
                    STATUS {report.id}
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline btn-sm btn-success-light"
                    onClick={() => handleSimulateReply('FIXED')}
                    disabled={simLoading}
                  >
                    FIXED {report.id}
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline btn-sm btn-danger-light"
                    onClick={() => handleSimulateReply('NOT_FIXED')}
                    disabled={simLoading}
                  >
                    NOT_FIXED {report.id}
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline btn-sm"
                    onClick={() => handleSimulateReply('HELP')}
                    disabled={simLoading}
                  >
                    HELP
                  </button>
                </div>

                <div className="sim-custom-box">
                  <input
                    type="text"
                    className="form-control form-control-sm"
                    placeholder={`e.g. FIXED ${report.id}`}
                    value={simBody}
                    onChange={(e) => setSimBody(e.target.value)}
                  />
                  <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    onClick={() => handleSimulateReply()}
                    disabled={simLoading || !simBody.trim()}
                  >
                    {simLoading ? 'Simulating...' : 'Send Reply'}
                  </button>
                </div>

                {simResult && (
                  <div className={`sim-result-box ${simResult.success ? 'sim-result-success' : 'sim-result-error'}`}>
                    <div><strong>Action Taken:</strong> {simResult.action || 'COMPLETED'}</div>
                    <div><strong>Bot Automated Reply:</strong></div>
                    <pre className="sim-bot-reply">{simResult.replyText}</pre>
                    {simResult.newStatus && (
                      <div><strong>Updated Issue Status:</strong> <span className="badge badge-info">{simResult.newStatus}</span></div>
                    )}
                  </div>
                )}
              </div>

              {/* Inbound Reply Log */}
              <div className="track-section">
                <h3>📥 Inbound Messages Received</h3>
                {report.inboundMessages && report.inboundMessages.length > 0 ? (
                  <div className="inbound-msg-list">
                    {report.inboundMessages.map((msg, idx) => (
                      <div key={idx} className="inbound-msg-item">
                        <div className="inbound-msg-header">
                          <span className="inbound-sender">From: {msg.fromPhone}</span>
                          <span className="inbound-time">{new Date(msg.receivedAt).toLocaleTimeString()}</span>
                        </div>
                        <div className="inbound-body">"{msg.rawBody}"</div>
                        <div className="inbound-footer">
                          Keyword: <code>{msg.keyword}</code> | Action: <strong>{msg.actionTaken}</strong>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-muted">No inbound replies recorded yet for this report.</p>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Quick Links */}
      <div className="track-footer-links">
        <Link to="/" className="btn btn-outline">← Back to Dashboard</Link>
        <Link to="/report" className="btn btn-primary">Report Another Issue →</Link>
      </div>
    </div>
  );
}
