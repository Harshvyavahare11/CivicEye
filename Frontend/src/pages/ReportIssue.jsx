import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ReportForm from '../components/reports/ReportForm';
import AnalysisCard from '../components/reports/AnalysisCard';
import Alert from '../components/common/Alert';
import { analyzeIssue } from '../services/aiService';
import { createReport } from '../services/reportService';

export function ReportIssue({ backendHealth, aiHealth }) {
  const navigate = useNavigate();

  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [error, setError] = useState(null);
  const [lastSubmissionParams, setLastSubmissionParams] = useState(null);

  const [isSaving, setIsSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(null);

  // Auto-suggest demo mode if AI Engine is not configured with live Gemma key
  const defaultDemo = Boolean(aiHealth && !aiHealth.gemmaKeyConfigured);

  const handleAnalyze = async ({ file, previewUrl, description, location, demoMode }) => {
    setIsAnalyzing(true);
    setError(null);
    setAnalysisResult(null);
    setSaveSuccess(null);

    setLastSubmissionParams({ file, previewUrl, description, location, demoMode });

    try {
      const result = await analyzeIssue(file, description, location, demoMode);
      setAnalysisResult({
        ...result,
        previewUrl,
        initialDescription: description,
        initialLocation: location
      });
    } catch (err) {
      setError(err.message || 'Analysis request failed.');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const handleRetry = () => {
    if (lastSubmissionParams) {
      handleAnalyze(lastSubmissionParams);
    }
  };

  const handleSaveReport = async (reportData) => {
    setIsSaving(true);
    setError(null);
    try {
      const saved = await createReport(reportData);
      setSaveSuccess(`Report ${saved.id} saved successfully! Redirecting to My Reports...`);
      setTimeout(() => {
        navigate('/reports');
      }, 1500);
    } catch (err) {
      setError(`Failed to save report: ${err.message}`);
      setIsSaving(false);
    }
  };

  return (
    <div className="page-container container" style={{ maxWidth: '850px' }}>
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.875rem', marginBottom: '0.35rem', color: 'var(--text-primary)' }}>
          Report a Civic Grievance
        </h1>
        <p style={{ color: 'var(--text-secondary)' }}>
          Upload a visible issue photograph. Google Gemma 4 will detect the problem, assess preliminary severity, identify the responsible department, and draft a verified complaint.
        </p>
      </div>

      {!backendHealth?.connected && (
        <Alert
          type="warning"
          title="Backend Disconnected"
          message="Spring Boot backend is currently unreachable at http://localhost:8080. Start the backend with 'mvn spring-boot:run' to enable AI analysis and report persistence."
        />
      )}

      {saveSuccess && (
        <Alert type="success" title="Success" message={saveSuccess} />
      )}

      <ReportForm
        onAnalyze={handleAnalyze}
        isAnalyzing={isAnalyzing}
        error={error}
        onRetry={handleRetry}
        defaultDemoMode={defaultDemo}
      />

      {analysisResult && (
        <AnalysisCard
          analysis={analysisResult}
          onSaveReport={handleSaveReport}
          isSaving={isSaving}
          initialLocation={analysisResult.initialLocation}
          initialDescription={analysisResult.initialDescription}
          imagePreviewUrl={analysisResult.previewUrl}
        />
      )}
    </div>
  );
}

export default ReportIssue;
