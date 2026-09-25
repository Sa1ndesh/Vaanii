/**
 * Hybrid Legal Service - Combines Online API with Offline LLM
 *
 * Strategy:
 * 1. If offline mode is loaded AND no internet → Use offline LLM
 * 2. If online AND complex query → Use online API (Gemini/Ollama)
 * 3. If online AND simple query AND offline loaded → Use offline for speed
 * 4. Graceful fallback between modes
 */

import { offlineLLM } from './offlineLegalLLM';
import { API_BASE_URL } from '../config';

class HybridLegalService {
  constructor() {
    this.preferOffline = false; // User preference
    this.offlineReady = false;

    // Listen for offline LLM status
    offlineLLM.addStatusListener(({ isLoaded }) => {
      this.offlineReady = isLoaded;
    });
  }

  /**
   * Check if device is online
   */
  isOnline() {
    return navigator.onLine;
  }

  /**
   * Set user preference for offline mode
   */
  setPreferOffline(prefer) {
    this.preferOffline = prefer;
    localStorage.setItem('vani_prefer_offline', prefer ? 'true' : 'false');
  }

  /**
   * Get user preference
   */
  getPreferOffline() {
    const stored = localStorage.getItem('vani_prefer_offline');
    return stored === 'true';
  }

  /**
   * Initialize offline mode
   */
  async initializeOffline(onProgress) {
    return await offlineLLM.initialize(onProgress);
  }

  /**
   * Determine which mode to use for a query
   */
  determineMode(query, forceMode = null) {
    if (forceMode === 'offline') return 'offline';
    if (forceMode === 'online') return 'online';

    // If no internet, must use offline
    if (!this.isOnline()) {
      return this.offlineReady ? 'offline' : 'unavailable';
    }

    // User prefers offline and it's ready
    if (this.preferOffline && this.offlineReady) {
      return 'offline';
    }

    // Default to online for best quality
    return 'online';
  }

  /**
   * Main chat function - automatically chooses mode
   */
  async chat(query, options = {}) {
    const {
      language = 'english',
      context = '',
      forceMode = null,
      onModeChange = () => {},
    } = options;

    const mode = this.determineMode(query, forceMode);
    onModeChange(mode);

    if (mode === 'unavailable') {
      return {
        success: false,
        error: 'No internet connection and offline mode not available',
        mode: 'unavailable'
      };
    }

    try {
      if (mode === 'offline') {
        const response = await offlineLLM.chat(query, language, context);
        return {
          success: true,
          response: response,
          mode: 'offline',
          disclaimer: 'Response generated offline. For complex legal matters, consult a qualified lawyer.'
        };
      } else {
        // Online mode - use existing API
        const response = await this.chatOnline(query, language);
        return {
          success: true,
          response: response.text || response.response,
          relevant_law: response.relevant_law,
          mode: 'online'
        };
      }
    } catch (error) {
      console.error(`${mode} mode failed:`, error);

      // Try fallback
      if (mode === 'online' && this.offlineReady) {
        console.log('Falling back to offline mode...');
        onModeChange('offline');
        try {
          const response = await offlineLLM.chat(query, language, context);
          return {
            success: true,
            response: response,
            mode: 'offline',
            fallback: true
          };
        } catch (offlineError) {
          return {
            success: false,
            error: 'Both online and offline modes failed',
            mode: 'error'
          };
        }
      }

      return {
        success: false,
        error: error.message,
        mode: 'error'
      };
    }
  }

  /**
   * Online chat via API
   */
  async chatOnline(query, language = 'english') {
    const response = await fetch(`${API_BASE_URL}/api/v1/chatbot/query`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ query, language }),
    });

    if (!response.ok) {
      throw new Error('API request failed');
    }

    return await response.json();
  }

  /**
   * Vani voice assistant - with offline support
   */
  async vaniAsk(query, options = {}) {
    const {
      language = 'english',
      state = '',
      district = '',
      forceMode = null,
    } = options;

    const mode = this.determineMode(query, forceMode);

    if (mode === 'offline' && this.offlineReady) {
      // Offline mode - use local LLM
      const response = await offlineLLM.chat(query, language);
      return {
        success: true,
        response: response,
        mode: 'offline',
        // TTS will need to use Web Speech API offline
        tts_available: 'speechSynthesis' in window
      };
    } else if (mode === 'online') {
      // Online mode - use API with dialect support
      const response = await fetch(`${API_BASE_URL}/api/vani/ask`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query, language, state, district }),
      });

      if (!response.ok) {
        throw new Error('Vani API failed');
      }

      return {
        success: true,
        ...(await response.json()),
        mode: 'online'
      };
    } else {
      return {
        success: false,
        error: 'Service unavailable',
        mode: 'unavailable'
      };
    }
  }

  /**
   * Analyze FIR - with offline support
   */
  async analyzeFIR(file, options = {}) {
    const { language = 'english', forceMode = null } = options;
    const mode = this.determineMode('fir analysis', forceMode);

    if (mode === 'offline' && this.offlineReady) {
      // For offline, we need to extract text first
      const text = await this.extractTextFromFile(file);
      if (!text) {
        return { success: false, error: 'Could not extract text from file' };
      }

      const analysis = await offlineLLM.analyzeFIR(text, language);
      return {
        success: true,
        analysis: analysis,
        mode: 'offline'
      };
    } else if (mode === 'online') {
      // Online mode - use API
      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch(`${API_BASE_URL}/api/v1/analyzer/analyze-fir`, {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('FIR analysis failed');
      }

      return {
        success: true,
        ...(await response.json()),
        mode: 'online'
      };
    }

    return { success: false, error: 'Service unavailable', mode: 'unavailable' };
  }

  /**
   * Summarize case - with offline support
   */
  async summarizeCase(file, options = {}) {
    const { language = 'english', forceMode = null } = options;
    const mode = this.determineMode('case summary', forceMode);

    if (mode === 'offline' && this.offlineReady) {
      const text = await this.extractTextFromFile(file);
      if (!text) {
        return { success: false, error: 'Could not extract text from file' };
      }

      const summary = await offlineLLM.summarizeCase(text, language);
      return {
        success: true,
        summary: summary,
        mode: 'offline'
      };
    } else if (mode === 'online') {
      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch(`${API_BASE_URL}/api/v1/summarizer/summarize`, {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('Case summarization failed');
      }

      return {
        success: true,
        ...(await response.json()),
        mode: 'online'
      };
    }

    return { success: false, error: 'Service unavailable', mode: 'unavailable' };
  }

  /**
   * Extract text from file (for offline processing)
   * Note: This is basic - for better extraction, use online API
   */
  async extractTextFromFile(file) {
    const type = file.type;

    if (type === 'text/plain') {
      return await file.text();
    }

    // For PDF/DOCX, we'd need additional libraries
    // In offline mode, we can only handle text files directly
    // For other formats, show a message to use online mode

    console.warn('Offline text extraction only supports .txt files');
    return null;
  }

  /**
   * Get current status
   */
  getStatus() {
    return {
      online: this.isOnline(),
      offlineReady: this.offlineReady,
      preferOffline: this.getPreferOffline(),
      currentMode: this.determineMode('')
    };
  }
}

// Singleton
export const hybridService = new HybridLegalService();
export default hybridService;
