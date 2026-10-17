import { request } from './api';

export async function getReports(filters = {}) {
  const query = new URLSearchParams();
  if (filters.category && filters.category !== 'all') {
    query.append('category', filters.category);
  }
  if (filters.status && filters.status !== 'all') {
    query.append('status', filters.status);
  }
  if (filters.search && filters.search.trim()) {
    query.append('search', filters.search.trim());
  }

  const endpoint = `/api/reports${query.toString() ? '?' + query.toString() : ''}`;
  return request(endpoint);
}

export async function getReportById(id) {
  return request(`/api/reports/${id}`);
}

export async function createReport(reportData) {
  return request('/api/reports', {
    method: 'POST',
    body: JSON.stringify(reportData)
  });
}

export async function updateReport(id, updateData) {
  return request(`/api/reports/${id}`, {
    method: 'PUT',
    body: JSON.stringify(updateData)
  });
}

export async function deleteReport(id) {
  return request(`/api/reports/${id}`, {
    method: 'DELETE'
  });
}

export async function getReportStats() {
  return request('/api/reports/stats');
}

export async function sendComplaint(id, recipientPhone, customNote) {
  return request(`/api/reports/${id}/send`, {
    method: 'POST',
    body: JSON.stringify({ recipientPhone, customNote })
  });
}

export async function simulateWebhook(from, body, reportId) {
  return request('/api/webhooks/messaging/simulate', {
    method: 'POST',
    body: JSON.stringify({ from, body, reportId })
  });
}

export default {
  getReports,
  getReportById,
  createReport,
  updateReport,
  deleteReport,
  getReportStats,
  sendComplaint,
  simulateWebhook
};
