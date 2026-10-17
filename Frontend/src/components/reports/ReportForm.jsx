import React, { useState, useRef } from 'react';
import { validateImageFile, formatFileSize } from '../../utils/validators';
import Alert from '../common/Alert';

export function ReportForm({ onAnalyze, isAnalyzing, error, onRetry, defaultDemoMode = false }) {
  const [file, setFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [demoMode, setDemoMode] = useState(defaultDemoMode);
  const [validationError, setValidationError] = useState(null);
  const [isGettingLocation, setIsGettingLocation] = useState(false);
  const [geoPrompt, setGeoPrompt] = useState(null);

  const fileInputRef = useRef(null);

  const handleFileSelect = (selectedFile) => {
    setValidationError(null);
    if (!selectedFile) return;

    const validation = validateImageFile(selectedFile);
    if (!validation.valid) {
      setValidationError(validation.error);
      return;
    }

    setFile(selectedFile);
    const objectUrl = URL.createObjectURL(selectedFile);
    setPreviewUrl(objectUrl);
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      handleFileSelect(e.target.files[0]);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileSelect(e.dataTransfer.files[0]);
    }
  };

  const handleRemoveImage = () => {
    setFile(null);
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }
    setPreviewUrl(null);
    setValidationError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleGetLocation = () => {
    if (!navigator.geolocation) {
      setValidationError('Geolocation is not supported by your browser.');
      return;
    }

    setGeoPrompt('CivicEye is requesting permission to access your GPS coordinates...');
    setIsGettingLocation(true);

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const lat = pos.coords.latitude.toFixed(5);
        const lng = pos.coords.longitude.toFixed(5);
        const geoText = `Lat: ${lat}, Long: ${lng}`;
        setLocation(geoText);
        setGeoPrompt(null);
        setIsGettingLocation(false);
      },
      (err) => {
        setGeoPrompt(null);
        setIsGettingLocation(false);
        setValidationError(`Location permission denied or unavailable (${err.message}). You may type the location manually.`);
      },
      { timeout: 10000 }
    );
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!file) {
      setValidationError('Please select or capture a photograph of the civic issue.');
      return;
    }

    onAnalyze({
      file,
      previewUrl,
      description,
      location,
      demoMode
    });
  };

  return (
    <form onSubmit={handleSubmit} className="card">
      <h2 style={{ fontSize: '1.25rem', marginBottom: '1rem', color: 'var(--text-primary)' }}>
        Submit Civic Issue for AI Analysis
      </h2>

      {validationError && (
        <Alert
          type="error"
          message={validationError}
          onDismiss={() => setValidationError(null)}
        />
      )}

      {error && (
        <div style={{ marginBottom: '1.25rem' }}>
          <Alert type="error" title="Analysis Error" message={error} />
          <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.5rem' }}>
            {onRetry && (
              <button type="button" className="btn btn-secondary" onClick={onRetry}>
                🔄 Retry Analysis
              </button>
            )}
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => {
                setDemoMode(true);
                if (onRetry) onRetry();
              }}
            >
              🧪 Switch to Demo Mode & Retry
            </button>
          </div>
        </div>
      )}

      {/* 1. Image Upload Control */}
      <div className="form-group">
        <label className="form-label">
          Civic Issue Photograph <span style={{ color: '#ef4444' }}>*</span>
        </label>

        {!previewUrl ? (
          <div
            className="dropzone"
            onDragOver={(e) => e.preventDefault()}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
          >
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              accept="image/jpeg,image/png,image/webp"
              style={{ display: 'none' }}
            />
            <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>📷</div>
            <div style={{ fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
              Click to upload photo, or drag and drop
            </div>
            <div className="form-help">
              Supports JPEG, PNG, and WebP (up to 8 MB)
            </div>
          </div>
        ) : (
          <div className="preview-container">
            <img src={previewUrl} alt="Issue preview" className="preview-image" />
            <div className="preview-overlay">
              <button
                type="button"
                className="btn btn-secondary"
                style={{ padding: '0.35rem 0.75rem', fontSize: '0.8125rem' }}
                onClick={() => fileInputRef.current?.click()}
              >
                Replace
              </button>
              <button
                type="button"
                className="btn btn-danger"
                style={{ padding: '0.35rem 0.75rem', fontSize: '0.8125rem' }}
                onClick={handleRemoveImage}
              >
                Remove
              </button>
            </div>
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              accept="image/jpeg,image/png,image/webp"
              style={{ display: 'none' }}
            />
          </div>
        )}

        {file && (
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
            <span>Selected: {file.name}</span>
            <span>Size: {formatFileSize(file.size)}</span>
          </div>
        )}
      </div>

      {/* 2. Issue Description */}
      <div className="form-group">
        <label className="form-label" htmlFor="description">
          Issue Description (Optional)
        </label>
        <textarea
          id="description"
          className="form-textarea"
          rows={3}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Describe visible landmarks, problem context, or how long this has persisted..."
          maxLength={1000}
        />
        <div className="form-help">
          The photo is analyzed directly. Add notes here if they help clarify the problem.
        </div>
      </div>

      {/* 3. Location Input */}
      <div className="form-group">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.375rem' }}>
          <label className="form-label" htmlFor="location" style={{ margin: 0 }}>
            Location & Landmark (Citizen Confirmed)
          </label>
          <button
            type="button"
            onClick={handleGetLocation}
            disabled={isGettingLocation}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--teal-600)',
              fontSize: '0.8125rem',
              fontWeight: 600,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.25rem'
            }}
          >
            📍 {isGettingLocation ? 'Detecting GPS...' : 'Use Current GPS'}
          </button>
        </div>

        {geoPrompt && (
          <div style={{ fontSize: '0.8125rem', color: 'var(--teal-700)', marginBottom: '0.5rem' }}>
            {geoPrompt}
          </div>
        )}

        <input
          id="location"
          type="text"
          className="form-input"
          value={location}
          onChange={(e) => setLocation(e.target.value)}
          placeholder="e.g. Near Nal Stop, Karve Road, Pune (or landmark/crossroad)"
        />
        <div className="form-help">
          We do not guess or fabricate addresses. Please verify or type the landmark yourself.
        </div>
      </div>

      {/* 4. Demo Mode Toggle */}
      <div style={{
        backgroundColor: 'var(--bg-main)',
        padding: '0.875rem 1rem',
        borderRadius: 'var(--radius-sm)',
        marginBottom: '1.5rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between'
      }}>
        <div>
          <label htmlFor="demo-toggle" style={{ fontWeight: 600, fontSize: '0.875rem', cursor: 'pointer', display: 'block' }}>
            🧪 Demo Mode (Simulated AI Analysis)
          </label>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            Test without consuming live Google Gemma 4 API quota.
          </div>
        </div>
        <input
          id="demo-toggle"
          type="checkbox"
          checked={demoMode}
          onChange={(e) => setDemoMode(e.target.checked)}
          style={{ width: '18px', height: '18px', cursor: 'pointer' }}
        />
      </div>

      {/* 5. Submit Button */}
      <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
        <button
          type="submit"
          className="btn btn-primary"
          disabled={!file || isAnalyzing}
          style={{ width: '100%', padding: '0.75rem 1.5rem', fontSize: '1rem' }}
        >
          {isAnalyzing ? (
            <>
              <span className="badge-dot" style={{ backgroundColor: 'white', animation: 'pulse 1s infinite' }} />
              Analyzing Issue with Gemma 4...
            </>
          ) : (
            '🔍 Analyze Issue with AI'
          )}
        </button>
      </div>
    </form>
  );
}

export default ReportForm;
