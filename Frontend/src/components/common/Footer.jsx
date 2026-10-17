import React from 'react';

export function Footer() {
  return (
    <footer style={{
      backgroundColor: 'var(--bg-surface)',
      borderTop: '1px solid var(--border-subtle)',
      padding: '2rem 0',
      color: 'var(--text-secondary)',
      fontSize: '0.875rem'
    }}>
      <div className="container" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
            CivicEye — AI Civic Issue Reporting System
          </div>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.8125rem' }}>
            V1 Prototype. Prepares and manages citizen complaints. Does not automatically submit to municipal authorities.
          </p>
        </div>
        <div style={{ textAlign: 'right', fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
          Powered by React • Spring Boot • FastAPI • Google Gemma 4
        </div>
      </div>
    </footer>
  );
}

export default Footer;
