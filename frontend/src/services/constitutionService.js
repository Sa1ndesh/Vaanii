import { API_BASE_URL } from '../config';

/**
 * Service for the Offline Constitution RAG Assistant.
 * Interacts with FastAPI backend without requiring external cloud services.
 */

export const askConstitution = async (question, topK = 3, language = 'en') => {
  const url = `${API_BASE_URL}/api/constitution/ask`;
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      question,
      top_k: topK,
      language,
    }),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.detail || `Server error (${response.status})`);
  }

  return await response.json();
};

export const getConstitutionStatus = async () => {
  const url = `${API_BASE_URL}/api/constitution/status`;
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to fetch status (${response.status})`);
  }

  return await response.json();
};

export const transcribeAudio = async (audioBlob) => {
  const url = `${API_BASE_URL}/api/voice/transcribe`;
  const formData = new FormData();
  formData.append('audio', audioBlob, 'recording.wav');

  const response = await fetch(url, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.detail || 'Voice transcription failed.');
  }

  return await response.json();
};

export const synthesizeSpeechAudioUrl = (text) => {
  return `${API_BASE_URL}/api/voice/synthesize`;
};
