import React from 'react';

export function StatusBadge({ status, type = 'status' }) {
  if (type === 'connection') {
    const isConnected = status === 'UP';
    return (
      <span className={`badge ${isConnected ? 'badge-connected' : 'badge-disconnected'}`}>
        <span className="badge-dot" />
        {isConnected ? 'Connected' : 'Disconnected'}
      </span>
    );
  }

  if (type === 'severity') {
    const sev = (status || 'Medium').toLowerCase();
    return (
      <span className={`badge sev-${sev}`}>
        {status} Severity
      </span>
    );
  }

  // Standard report lifecycle status
  let badgeClass = 'badge-demo';
  if (status === 'Submitted') badgeClass = 'badge-connected';
  else if (status === 'Resolved') badgeClass = 'sev-low';
  else if (status === 'Ready for Submission') badgeClass = 'sev-medium';

  return (
    <span className={`badge ${badgeClass}`}>
      {status || 'Draft'}
    </span>
  );
}

export default StatusBadge;
