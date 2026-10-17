import React, { useEffect, useState } from 'react';

export default function SplashScreen({ onFinish }) {
  const [fadingOut, setFadingOut] = useState(false);

  useEffect(() => {
    // Respect reduced-motion preference
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const duration = prefersReducedMotion ? 400 : 1800;

    const fadeTimer = setTimeout(() => {
      setFadingOut(true);
    }, duration);

    const finishTimer = setTimeout(() => {
      if (onFinish) onFinish();
    }, duration + 400);

    return () => {
      clearTimeout(fadeTimer);
      clearTimeout(finishTimer);
    };
  }, [onFinish]);

  return (
    <div className={`splash-overlay ${fadingOut ? 'splash-fade-out' : ''}`}>
      <div className="splash-card">
        <div className="splash-brand">
          <div className="splash-logo-ring">
            <span className="splash-logo-icon">👁️</span>
          </div>
          <h1 className="splash-title">Civic<span className="splash-accent">Eye</span></h1>
          <p className="splash-tagline">AI-Powered Civic Issue Reporting &amp; Follow-Up</p>
        </div>

        <div className="splash-loader-bar">
          <div className="splash-loader-progress" />
        </div>

        <div className="splash-status-text">
          <span>Initializing civic intelligence engine...</span>
        </div>

        <button
          type="button"
          className="splash-skip-btn"
          onClick={() => {
            setFadingOut(true);
            setTimeout(() => onFinish && onFinish(), 200);
          }}
        >
          Skip to Dashboard &rarr;
        </button>
      </div>
    </div>
  );
}
