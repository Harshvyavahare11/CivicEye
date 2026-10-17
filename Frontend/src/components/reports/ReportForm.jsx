import React, { useState, useRef } from 'react';
import { validateImageFile, formatFileSize } from '../../utils/validators';
import Alert from '../common/Alert';
import LocationMap from '../common/LocationMap';

export function ReportForm({ onAnalyze, isAnalyzing, error, onRetry, defaultDemoMode = false }) {
  const [file, setFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [latitude, setLatitude] = useState(18.5204);
  const [longitude, setLongitude] = useState(73.8567);
  const [recipientPhone, setRecipientPhone] = useState('+917058207967');
  const [demoMode, setDemoMode] = useState(defaultDemoMode);
  const [validationError, setValidationError] = useState(null);

  const fileInputRef = useRef(null);
  const cameraInputRef = useRef(null);

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
    if (fileInputRef.current) fileInputRef.current.value = '';
    if (cameraInputRef.current) cameraInputRef.current.value = '';
  };

  const handleLocationChange = ({ latitude: lat, longitude: lng, address }) => {
    setLatitude(lat);
    setLongitude(lng);
    setLocation(address);
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
      latitude,
      longitude,
      recipientPhone,
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
              🧪 Switch to Demo Mode &amp; Retry
            </button>
          </div>
        </div>
      )}

      {/* 1. Image Upload & Camera Control */}
      <div className="form-group">
        <label className="form-label">
          Civic Issue Photograph <span style={{ color: '#ef4444' }}>*</span>
        </label>

        {!previewUrl ? (
          <div>
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
              <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>📸</div>
              <div style={{ fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
                Click to upload file, or drag and drop
              </div>
              <div className="form-help">
                Supports JPEG, PNG, and WebP (up to 8 MB)
              </div>
            </div>

            {/* Device Camera Input */}
            <div style={{ marginTop: '0.75rem', display: 'flex', justifyContent: 'center' }}>
              <input
                type="file"
                ref={cameraInputRef}
                onChange={handleFileChange}
                accept="image/*"
                capture="environment"
                style={{ display: 'none' }}
              />
              <button
                type="button"
                className="btn btn-outline btn-sm"
                onClick={() => cameraInputRef.current?.click()}
              >
                📷 Take Photo with Camera
              </button>
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

      {/* 3. Interactive Location Map & Address */}
      <div className="form-group">
        <label className="form-label">
          Civic Location &amp; Coordinates (OpenStreetMap)
        </label>
        <LocationMap
          latitude={latitude}
          longitude={longitude}
          address={location}
          onLocationChange={handleLocationChange}
          readOnly={false}
          height="240px"
        />

        <div style={{ marginTop: '0.75rem' }}>
          <label className="form-label" htmlFor="manual-location" style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
            Location Landmark / Address (editable):
          </label>
          <input
            id="manual-location"
            type="text"
            className="form-input"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            placeholder="e.g. Near Metro Pillar 42, MG Road, Pune"
          />
        </div>
      </div>

      {/* 4. Recipient Phone Contact */}
      <div className="form-group">
        <label className="form-label" htmlFor="recipient-phone">
          🏛️ Verified Government / Municipal Department Contact (SMS Dispatch)
        </label>
        <input
          id="recipient-phone"
          type="text"
          className="form-input"
          value={recipientPhone}
          onChange={(e) => setRecipientPhone(e.target.value)}
          placeholder="+917058207967 or verified municipal contact"
        />
        <div className="form-help" style={{ fontSize: '0.8rem', color: '#b45309', marginTop: '0.35rem' }}>
          ℹ️ <strong>Important:</strong> This sends an SMS from your authorized Twilio sender. It does not automatically register an official government complaint. The department must agree to receive and process complaints through that channel.
        </div>
      </div>

      {/* 5. Demo Mode Toggle */}
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

      {/* 6. Submit Button */}
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
