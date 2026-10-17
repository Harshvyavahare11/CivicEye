import { request } from './api';

export async function checkBackendHealth() {
  return request('/api/health');
}

export async function checkAiHealth() {
  return request('/api/ai/health');
}

export async function analyzeIssue(imageFile, description, location, demoMode = false) {
  const formData = new FormData();
  formData.append('image', imageFile);
  if (description && description.trim()) {
    formData.append('description', description.trim());
  }
  if (location && location.trim()) {
    formData.append('location', location.trim());
  }
  formData.append('demoMode', demoMode);

  return request('/api/reports/analyze', {
    method: 'POST',
    body: formData
  });
}

export default {
  checkBackendHealth,
  checkAiHealth,
  analyzeIssue
};
