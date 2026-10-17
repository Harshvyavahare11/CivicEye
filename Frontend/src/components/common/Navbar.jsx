import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import StatusBadge from './StatusBadge';

export function Navbar({ backendHealth, aiHealth }) {
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <Link to="/" className="brand">
          <div className="brand-icon">CE</div>
          <span>CivicEye</span>
        </Link>

        <nav>
          <ul className="nav-links">
            <li>
              <Link to="/" className={`nav-link ${isActive('/') ? 'active' : ''}`}>
                Dashboard
              </Link>
            </li>
            <li>
              <Link to="/report" className={`nav-link ${isActive('/report') ? 'active' : ''}`}>
                Report an Issue
              </Link>
            </li>
            <li>
              <Link to="/reports" className={`nav-link ${isActive('/reports') ? 'active' : ''}`}>
                My Reports
              </Link>
            </li>
            <li>
              <Link to="/about" className={`nav-link ${isActive('/about') ? 'active' : ''}`}>
                About & AI
              </Link>
            </li>
          </ul>
        </nav>

        <div className="nav-status-group">
          {/* Backend indicator */}
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', fontSize: '0.75rem' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.7rem' }}>Backend:</span>
            <StatusBadge status={backendHealth?.status} type="connection" />
          </div>

          {/* AI Engine indicator */}
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', fontSize: '0.75rem' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.7rem' }}>AI Engine:</span>
            <span className={`badge ${aiHealth?.connected ? (aiHealth?.gemmaKeyConfigured ? 'badge-connected' : 'badge-demo') : 'badge-disconnected'}`}>
              <span className="badge-dot" />
              {aiHealth?.connected
                ? (aiHealth?.gemmaKeyConfigured ? 'Gemma 4 Live' : 'Demo Available')
                : 'Offline'}
            </span>
          </div>

          <Link to="/report" className="btn btn-primary" style={{ padding: '0.45rem 1rem', fontSize: '0.875rem' }}>
            + Report Issue
          </Link>
        </div>
      </div>
    </header>
  );
}

export default Navbar;
