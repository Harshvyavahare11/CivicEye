import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { getReports, deleteReport, updateReport } from '../services/reportService';
import StatusBadge from '../components/common/StatusBadge';
import Alert from '../components/common/Alert';
import { formatDate } from '../utils/validators';

export function MyReports() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedStatus, setSelectedStatus] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');

  // Delete modal state
  const [reportToDelete, setReportToDelete] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Quick edit draft modal state
  const [editingReport, setEditingReport] = useState(null);
  const [draftText, setDraftText] = useState('');
  const [draftStatus, setDraftStatus] = useState('Ready for Submission');
  const [isUpdating, setIsUpdating] = useState(false);

  const fetchReports = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getReports({
        category: selectedCategory,
        status: selectedStatus,
        search: searchQuery
      });
      setReports(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Failed to load reports.');
    } finally {
      setLoading(false);
    }
  }, [selectedCategory, selectedStatus, searchQuery]);

  useEffect(() => {
    fetchReports();
  }, [fetchReports]);

  const handleDeleteConfirm = async () => {
    if (!reportToDelete) return;
    setIsDeleting(true);
    try {
      await deleteReport(reportToDelete.id);
      setReports(prev => prev.filter(r => r.id !== reportToDelete.id));
      setReportToDelete(null);
    } catch (err) {
      setError(`Failed to delete report: ${err.message}`);
    } finally {
      setIsDeleting(false);
    }
  };

  const handleOpenEdit = (report) => {
    setEditingReport(report);
    setDraftText(report.complaintText || '');
    setDraftStatus(report.status || 'Ready for Submission');
  };

  const handleSaveEdit = async () => {
    if (!editingReport) return;
    setIsUpdating(true);
    try {
      const updated = await updateReport(editingReport.id, {
        complaintText: draftText,
        status: draftStatus
      });
      setReports(prev => prev.map(r => r.id === updated.id ? updated : r));
      setEditingReport(null);
    } catch (err) {
      setError(`Failed to update report: ${err.message}`);
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <div className="page-container container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.875rem', marginBottom: '0.25rem', color: 'var(--text-primary)' }}>
            My Civic Reports
          </h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Track, review, customize complaint drafts, and manage grievance statuses.
          </p>
        </div>

        <Link to="/report" className="btn btn-primary">
          + Report New Issue
        </Link>
      </div>

      {error && (
        <Alert type="error" title="Notice" message={error} onDismiss={() => setError(null)} />
      )}

      {/* Filter and Search Bar */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem' }}>
        <div className="grid-3" style={{ alignItems: 'flex-end' }}>
          <div>
            <label className="form-label" style={{ fontSize: '0.8125rem' }}>Search Keywords or ID</label>
            <input
              type="text"
              className="form-input"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search title, location, ID..."
            />
          </div>

          <div>
            <label className="form-label" style={{ fontSize: '0.8125rem' }}>Filter by Category</label>
            <select
              className="form-select"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
            >
              <option value="all">All Categories</option>
              <option value="Pothole">Potholes & Roads</option>
              <option value="Garbage">Garbage & Waste</option>
              <option value="Drainage">Drainage & Sewerage</option>
              <option value="Waterlogging">Waterlogging</option>
              <option value="Streetlight">Streetlights</option>
              <option value="Other">Other Issues</option>
            </select>
          </div>

          <div>
            <label className="form-label" style={{ fontSize: '0.8125rem' }}>Filter by Status</label>
            <select
              className="form-select"
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
            >
              <option value="all">All Statuses</option>
              <option value="Draft">Draft</option>
              <option value="Ready for Submission">Ready for Submission</option>
              <option value="Submitted">Submitted (Citizen Marked)</option>
              <option value="Resolved">Resolved</option>
            </select>
          </div>
        </div>
      </div>

      {/* List / Table of Reports */}
      {loading ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
          Loading your reports...
        </div>
      ) : reports.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '3.5rem' }}>
          <div style={{ fontSize: '3rem', marginBottom: '0.75rem' }}>📭</div>
          <h2 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>No matching reports found</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', fontSize: '0.9375rem' }}>
            Try adjusting your search criteria or create a new civic report.
          </p>
          <Link to="/report" className="btn btn-primary">
            Report a Civic Issue
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
                  <th style={{ padding: '0.875rem 1.25rem' }}>Created</th>
                  <th style={{ padding: '0.875rem 1.25rem', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {reports.map((report) => (
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
                      <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                        <Link to={`/reports/${report.id}`} className="btn btn-secondary" style={{ padding: '0.35rem 0.65rem', fontSize: '0.75rem' }}>
                          View
                        </Link>
                        <button
                          type="button"
                          className="btn btn-secondary"
                          style={{ padding: '0.35rem 0.65rem', fontSize: '0.75rem' }}
                          onClick={() => handleOpenEdit(report)}
                        >
                          Edit Draft
                        </button>
                        <button
                          type="button"
                          className="btn btn-danger"
                          style={{ padding: '0.35rem 0.65rem', fontSize: '0.75rem' }}
                          onClick={() => setReportToDelete(report)}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Quick Edit Draft Modal */}
      {editingReport && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(15, 23, 42, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '1rem'
        }}>
          <div className="card" style={{ maxWidth: '600px', width: '100%' }}>
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>
              Edit Complaint Draft ({editingReport.id})
            </h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.8125rem', marginBottom: '1rem' }}>
              Modify the grievance text or update the report lifecycle status.
            </p>

            <div className="form-group">
              <label className="form-label">Complaint Text</label>
              <textarea
                className="form-textarea"
                rows={4}
                value={draftText}
                onChange={(e) => setDraftText(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Report Status</label>
              <select
                className="form-select"
                value={draftStatus}
                onChange={(e) => setDraftStatus(e.target.value)}
              >
                <option value="Draft">Draft</option>
                <option value="Ready for Submission">Ready for Submission</option>
                <option value="Submitted">Submitted (Citizen Marked)</option>
                <option value="Resolved">Resolved</option>
              </select>
              <div className="form-help">
                * Note: Marking as Submitted does not automatically send to municipal departments in V1.
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setEditingReport(null)}
                disabled={isUpdating}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleSaveEdit}
                disabled={isUpdating}
              >
                {isUpdating ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {reportToDelete && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(15, 23, 42, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '1rem'
        }}>
          <div className="card" style={{ maxWidth: '450px', width: '100%' }}>
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem', color: '#991b1b' }}>
              Confirm Report Deletion
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9375rem', marginBottom: '1.5rem' }}>
              Are you sure you want to delete report <strong>{reportToDelete.id}</strong> ("{reportToDelete.title}")? This action cannot be undone.
            </p>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setReportToDelete(null)}
                disabled={isDeleting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-danger"
                onClick={handleDeleteConfirm}
                disabled={isDeleting}
              >
                {isDeleting ? 'Deleting...' : 'Yes, Delete Report'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default MyReports;
