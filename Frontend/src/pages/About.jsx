import React from 'react';

export function About() {
  return (
    <div className="page-container container" style={{ maxWidth: '800px' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem', color: 'var(--text-primary)' }}>
          About CivicEye & Responsible AI
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1.0625rem' }}>
          Understanding our architecture, design principles, ethical AI commitments, and prototype limitations.
        </p>
      </div>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.25rem', marginBottom: '0.75rem', color: 'var(--teal-700)' }}>
          1. Purpose of CivicEye
        </h2>
        <p style={{ color: 'var(--text-secondary)', lineHeight: 1.6, marginBottom: '1rem' }}>
          CivicEye is an AI-powered municipal reporting platform built to bridge the gap between community members witnessing infrastructure decay and the public works authorities responsible for repairs. By leveraging computer vision and Google Gemma 4, CivicEye assists citizens in accurately categorizing issues, identifying responsible agencies, and articulating concise, respectful grievance reports.
        </p>
      </div>

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.25rem', marginBottom: '0.75rem', color: '#9a3412' }}>
          2. Responsible AI Principles & Disclaimers
        </h2>
        <ul style={{ paddingLeft: '1.25rem', color: 'var(--text-secondary)', lineHeight: 1.7 }}>
          <li style={{ marginBottom: '0.5rem' }}>
            <strong>AI Makes Mistakes:</strong> Visual models can misidentify materials, depth, or lighting reflections. Every output is presented as an editable recommendation.
          </li>
          <li style={{ marginBottom: '0.5rem' }}>
            <strong>Preliminary Severity Assessment:</strong> Severities ("Low", "Medium", "High", "Critical") are preliminary estimates based purely on photograph visibility. They do not substitute for on-site structural engineering assessments.
          </li>
          <li style={{ marginBottom: '0.5rem' }}>
            <strong>Human-in-the-Loop Requirement:</strong> Citizens must verify and edit the generated complaint draft, confirmed location, and department before saving.
          </li>
          <li style={{ marginBottom: '0.5rem' }}>
            <strong>Explicit Location Privacy:</strong> CivicEye never guesses or fabricates locations. Browser geolocation is requested with explicit user consent and can always be reviewed or typed manually.
          </li>
          <li>
            <strong>No Automatic Government Submission in V1:</strong> CivicEye V1 prepares and manages reports locally. It does not claim that a report has been officially dispatched to a municipality unless a certified government gateway is integrated.
          </li>
        </ul>
      </div>

      <div className="card">
        <h2 style={{ fontSize: '1.25rem', marginBottom: '0.75rem', color: 'var(--teal-700)' }}>
          3. Technical Architecture
        </h2>
        <div style={{ color: 'var(--text-secondary)', lineHeight: 1.6 }}>
          <p style={{ marginBottom: '0.75rem' }}>
            CivicEye is engineered with a strict 3-tier decoupling:
          </p>
          <ul style={{ paddingLeft: '1.25rem', marginBottom: '0.75rem' }}>
            <li><strong>Frontend (Port 5173):</strong> React, Vite, React Router, responsive CSS.</li>
            <li><strong>Backend API (Port 8080):</strong> Java 21, Spring Boot, Bean Validation, thread-safe persistence.</li>
            <li><strong>AI Engine (Port 8000):</strong> Python 3.14, FastAPI, hosted Google Gemma 4 API, Pydantic validation, and simulated Demo Mode.</li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default About;
