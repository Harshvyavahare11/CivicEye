const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export async function request(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  const defaultHeaders = {};

  // If not FormData, default to JSON
  if (!(options.body instanceof FormData)) {
    defaultHeaders['Content-Type'] = 'application/json';
    defaultHeaders['Accept'] = 'application/json';
  } else {
    defaultHeaders['Accept'] = 'application/json';
  }

  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers
    }
  };

  try {
    const response = await fetch(url, config);
    
    // Check for 204 No Content
    if (response.status === 204) {
      return { success: true };
    }

    const contentType = response.headers.get('content-type');
    const isJson = contentType && contentType.includes('application/json');
    const data = isJson ? await response.json() : await response.text();

    if (!response.ok) {
      const errorMsg = (typeof data === 'object' && data.message)
        ? data.message
        : `Request failed with status ${response.status}`;
      const err = new Error(errorMsg);
      err.status = response.status;
      err.data = data;
      throw err;
    }

    return data;
  } catch (err) {
    if (err.name === 'TypeError' && err.message.includes('fetch')) {
      const networkErr = new Error('Cannot connect to CivicEye Backend. Please verify Spring Boot is running on ' + API_BASE_URL);
      networkErr.status = 0;
      throw networkErr;
    }
    throw err;
  }
}

export default {
  API_BASE_URL,
  request
};
