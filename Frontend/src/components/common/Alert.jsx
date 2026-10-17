import React from 'react';

export function Alert({ type = 'info', message, title, children, onDismiss }) {
  if (!message && !children) return null;

  return (
    <div className={`alert alert-${type}`} role="alert">
      <div style={{ flex: 1 }}>
        {title && <div style={{ fontWeight: 700, marginBottom: '0.25rem' }}>{title}</div>}
        <div>{message || children}</div>
      </div>
      {onDismiss && (
        <button
          onClick={onDismiss}
          style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.25rem', opacity: 0.6 }}
          aria-label="Dismiss alert"
        >
          ×
        </button>
      )}
    </div>
  );
}

export default Alert;
