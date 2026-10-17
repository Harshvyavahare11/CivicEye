import { useState, useEffect, useCallback } from 'react';
import { checkBackendHealth, checkAiHealth } from '../services/aiService';

export function useServiceHealth(pollIntervalMs = 20000) {
  const [backendHealth, setBackendHealth] = useState({
    status: 'CHECKING',
    service: 'CivicEye Backend',
    connected: false,
    error: null
  });

  const [aiHealth, setAiHealth] = useState({
    status: 'CHECKING',
    service: 'CivicEye AI Engine',
    connected: false,
    gemmaKeyConfigured: false,
    model: 'gemma-4-26b-a4b-it',
    message: 'Checking AI engine...',
    error: null
  });

  const [isChecking, setIsChecking] = useState(false);

  const checkHealth = useCallback(async () => {
    setIsChecking(true);

    // 1. Check Spring Boot Backend
    try {
      const bRes = await checkBackendHealth();
      setBackendHealth({
        status: bRes.status || 'UP',
        service: bRes.service || 'CivicEye Backend',
        connected: bRes.status === 'UP',
        error: null
      });

      // 2. Check AI Engine through Spring Boot
      try {
        const aiRes = await checkAiHealth();
        setAiHealth({
          status: aiRes.status || 'UP',
          service: aiRes.service || 'CivicEye AI Engine',
          connected: Boolean(aiRes.connected),
          gemmaKeyConfigured: Boolean(aiRes.gemmaKeyConfigured),
          model: aiRes.model || 'gemma-4-26b-a4b-it',
          message: aiRes.message || 'Operational',
          error: null
        });
      } catch (aiErr) {
        setAiHealth({
          status: 'DOWN',
          service: 'CivicEye AI Engine',
          connected: false,
          gemmaKeyConfigured: false,
          model: 'unavailable',
          message: 'AI Engine unreachable via backend',
          error: aiErr.message
        });
      }
    } catch (bErr) {
      setBackendHealth({
        status: 'DOWN',
        service: 'CivicEye Backend',
        connected: false,
        error: bErr.message
      });
      setAiHealth(prev => ({
        ...prev,
        status: 'UNKNOWN',
        connected: false,
        message: 'Backend is disconnected'
      }));
    } finally {
      setIsChecking(false);
    }
  }, []);

  useEffect(() => {
    checkHealth();
    if (pollIntervalMs > 0) {
      const interval = setInterval(checkHealth, pollIntervalMs);
      return () => clearInterval(interval);
    }
  }, [checkHealth, pollIntervalMs]);

  return {
    backendHealth,
    aiHealth,
    isChecking,
    refreshHealth: checkHealth
  };
}

export default useServiceHealth;
