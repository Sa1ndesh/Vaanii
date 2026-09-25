/**
 * Offline Legal LLM Service for Vani-Kanoon
 * Uses llama.cpp via Capacitor for on-device inference
 *
 * This service provides:
 * - On-device legal Q&A
 * - Document analysis
 * - Multilingual support (English, Hindi, Kannada, Marathi)
 * - Graceful fallback to online API when needed
 */

import { Capacitor } from '@capacitor/core';

// Check if we're running on native platform
const isNative = Capacitor.isNativePlatform();

// Dynamic import for Capacitor Llama (only available on native)
let CapacitorLlama = null;

if (isNative) {
  import('@nicepkg/capacitor-llama').then(module => {
    CapacitorLlama = module.CapacitorLlama;
  }).catch(err => {
    console.warn('Capacitor Llama not available:', err);
  });
}

// System prompts for different languages
const SYSTEM_PROMPTS = {
  english: `You are Vani, an expert AI legal assistant specializing in Indian law. You provide accurate, helpful information about Indian laws including:
- Bharatiya Nyaya Sanhita (BNS) 2023 - replaced IPC
- Bharatiya Nagarik Suraksha Sanhita (BNSS) 2023 - replaced CrPC
- Bharatiya Sakshya Adhiniyam (BSA) 2023 - replaced Evidence Act
- Constitution of India
- All other Central and State Acts

Always cite relevant sections and acts. Be concise but thorough. If you're unsure, say so rather than providing incorrect information.`,

  hindi: `आप वाणी हैं, भारतीय कानून में विशेषज्ञ AI कानूनी सहायक। आप भारतीय कानूनों के बारे में सटीक, सहायक जानकारी प्रदान करते हैं जिसमें शामिल हैं:
- भारतीय न्याय संहिता (BNS) 2023 - IPC का स्थान लिया
- भारतीय नागरिक सुरक्षा संहिता (BNSS) 2023 - CrPC का स्थान लिया
- भारतीय साक्ष्य अधिनियम (BSA) 2023 - साक्ष्य अधिनियम का स्थान लिया
- भारत का संविधान
- अन्य सभी केंद्रीय और राज्य अधिनियम

हमेशा संबंधित धाराओं और अधिनियमों का हवाला दें। संक्षिप्त लेकिन पूर्ण रहें।`,

  kannada: `ನೀವು ವಾಣಿ, ಭಾರತೀಯ ಕಾನೂನಿನಲ್ಲಿ ಪರಿಣಿತ AI ಕಾನೂನು ಸಹಾಯಕ. ನೀವು ಭಾರತೀಯ ಕಾನೂನುಗಳ ಬಗ್ಗೆ ನಿಖರವಾದ, ಸಹಾಯಕ ಮಾಹಿತಿಯನ್ನು ನೀಡುತ್ತೀರಿ:
- ಭಾರತೀಯ ನ್ಯಾಯ ಸಂಹಿತೆ (BNS) 2023
- ಭಾರತೀಯ ನಾಗರಿಕ ಸುರಕ್ಷಾ ಸಂಹಿತೆ (BNSS) 2023
- ಭಾರತೀಯ ಸಾಕ್ಷ್ಯ ಅಧಿನಿಯಮ (BSA) 2023
- ಭಾರತದ ಸಂವಿಧಾನ

ಸಂಬಂಧಿತ ವಿಭಾಗಗಳು ಮತ್ತು ಕಾಯಿದೆಗಳನ್ನು ಯಾವಾಗಲೂ ಉಲ್ಲೇಖಿಸಿ.`,

  marathi: `तुम्ही वाणी आहात, भारतीय कायद्यात तज्ञ AI कायदेशीर सहाय्यक. तुम्ही भारतीय कायद्यांबद्दल अचूक, उपयुक्त माहिती देता:
- भारतीय न्याय संहिता (BNS) 2023
- भारतीय नागरिक सुरक्षा संहिता (BNSS) 2023
- भारतीय साक्ष्य अधिनियम (BSA) 2023
- भारताचे संविधान

संबंधित कलमे आणि कायद्यांचा नेहमी संदर्भ द्या.`
};

// Model configuration
const MODEL_CONFIG = {
  modelPath: 'models/vani-legal-q4_k_m.gguf',
  contextLength: 2048,
  threads: 4,
  batchSize: 512,
  temperature: 0.7,
  topK: 40,
  topP: 0.95,
  repeatPenalty: 1.1,
};

class OfflineLegalLLM {
  constructor() {
    this.isLoaded = false;
    this.isLoading = false;
    this.loadError = null;
    this.listeners = new Set();
  }

  /**
   * Check if offline mode is available
   */
  isAvailable() {
    return isNative && CapacitorLlama !== null;
  }

  /**
   * Add listener for load status changes
   */
  addStatusListener(callback) {
    this.listeners.add(callback);
    return () => this.listeners.delete(callback);
  }

  /**
   * Notify listeners of status change
   */
  notifyListeners() {
    this.listeners.forEach(cb => cb({
      isLoaded: this.isLoaded,
      isLoading: this.isLoading,
      error: this.loadError
    }));
  }

  /**
   * Initialize the offline LLM
   * @param {function} onProgress - Progress callback (0-100)
   */
  async initialize(onProgress = () => {}) {
    if (this.isLoaded) {
      console.log('Offline LLM already loaded');
      return true;
    }

    if (this.isLoading) {
      console.log('Offline LLM already loading...');
      return false;
    }

    if (!this.isAvailable()) {
      console.log('Offline LLM not available on this platform');
      return false;
    }

    this.isLoading = true;
    this.loadError = null;
    this.notifyListeners();

    try {
      console.log('Loading offline legal LLM...');
      onProgress(0);

      await CapacitorLlama.loadModel({
        modelPath: MODEL_CONFIG.modelPath,
        nCtx: MODEL_CONFIG.contextLength,
        nThreads: MODEL_CONFIG.threads,
        nBatch: MODEL_CONFIG.batchSize,
        useMmap: true,
        useMlock: false,
        vocabOnly: false,
      });

      onProgress(100);
      this.isLoaded = true;
      this.isLoading = false;
      this.notifyListeners();

      console.log('✅ Offline legal LLM loaded successfully');
      return true;

    } catch (error) {
      console.error('Failed to load offline LLM:', error);
      this.loadError = error.message || 'Failed to load model';
      this.isLoading = false;
      this.notifyListeners();
      return false;
    }
  }

  /**
   * Unload the model to free memory
   */
  async unload() {
    if (!this.isLoaded) return;

    try {
      await CapacitorLlama.unloadModel();
      this.isLoaded = false;
      this.notifyListeners();
      console.log('Offline LLM unloaded');
    } catch (error) {
      console.error('Error unloading model:', error);
    }
  }

  /**
   * Build the prompt for the model
   */
  buildPrompt(userMessage, language = 'english', context = '') {
    const systemPrompt = SYSTEM_PROMPTS[language] || SYSTEM_PROMPTS.english;

    let fullPrompt = `<|system|>\n${systemPrompt}<|end|>\n<|user|>\n`;

    if (context) {
      fullPrompt += `Context:\n${context}\n\n`;
    }

    fullPrompt += `${userMessage}<|end|>\n<|assistant|>\n`;

    return fullPrompt;
  }

  /**
   * Send a chat message and get response
   * @param {string} userMessage - User's question
   * @param {string} language - Language code (english, hindi, kannada, marathi)
   * @param {string} context - Optional context (e.g., document text)
   * @returns {Promise<string>} - Model's response
   */
  async chat(userMessage, language = 'english', context = '') {
    if (!this.isLoaded) {
      throw new Error('Model not loaded. Call initialize() first.');
    }

    const prompt = this.buildPrompt(userMessage, language, context);

    try {
      const result = await CapacitorLlama.completion({
        prompt: prompt,
        nPredict: 1024,
        temperature: MODEL_CONFIG.temperature,
        topK: MODEL_CONFIG.topK,
        topP: MODEL_CONFIG.topP,
        repeatPenalty: MODEL_CONFIG.repeatPenalty,
        stop: ['<|end|>', '<|user|>', '<|system|>'],
      });

      return result.text.trim();

    } catch (error) {
      console.error('Inference error:', error);
      throw new Error('Failed to generate response: ' + error.message);
    }
  }

  /**
   * Stream chat response (token by token)
   * @param {string} userMessage - User's question
   * @param {string} language - Language code
   * @param {function} onToken - Callback for each token
   * @returns {Promise<string>} - Complete response
   */
  async chatStream(userMessage, language = 'english', onToken = () => {}) {
    if (!this.isLoaded) {
      throw new Error('Model not loaded');
    }

    const prompt = this.buildPrompt(userMessage, language);
    let fullResponse = '';

    try {
      await CapacitorLlama.completionStream({
        prompt: prompt,
        nPredict: 1024,
        temperature: MODEL_CONFIG.temperature,
        topK: MODEL_CONFIG.topK,
        topP: MODEL_CONFIG.topP,
        repeatPenalty: MODEL_CONFIG.repeatPenalty,
        stop: ['<|end|>', '<|user|>'],
        onToken: (token) => {
          fullResponse += token;
          onToken(token, fullResponse);
        }
      });

      return fullResponse.trim();

    } catch (error) {
      console.error('Streaming error:', error);
      throw error;
    }
  }

  /**
   * Analyze an FIR document
   */
  async analyzeFIR(firText, language = 'english') {
    const prompt = `Analyze this FIR document and extract the following information in JSON format:
- fir_number: FIR number
- police_station: Police station name
- district: District
- date_of_fir: Date of FIR filing
- date_of_offense: Date of offense
- complainant_name: Complainant's name
- complainant_address: Complainant's address
- accused_name: Name(s) of accused
- sections_applied: IPC/BNS sections applied
- brief_facts: Brief description of the incident
- witnesses: Names of witnesses (if mentioned)

FIR Document:
${firText.substring(0, 3000)}

Respond with valid JSON only.`;

    const response = await this.chat(prompt, language);

    // Try to parse JSON from response
    try {
      const jsonMatch = response.match(/\{[\s\S]*\}/);
      if (jsonMatch) {
        return JSON.parse(jsonMatch[0]);
      }
    } catch (e) {
      console.warn('Could not parse FIR analysis as JSON');
    }

    return { raw_analysis: response };
  }

  /**
   * Summarize a legal case/judgment
   */
  async summarizeCase(caseText, language = 'english') {
    const prompt = `Summarize this legal case/judgment with the following structure:

1. Case Title & Citation
2. Court
3. Date of Judgment
4. Parties (Petitioner vs Respondent)
5. Key Issues
6. Arguments (Brief)
7. Holding/Decision
8. Legal Principles Established
9. Sections/Laws Cited

Case Document:
${caseText.substring(0, 4000)}

Provide a comprehensive but concise summary.`;

    return await this.chat(prompt, language);
  }

  /**
   * Generate a legal document draft
   */
  async generateDocument(docType, details, language = 'english') {
    const prompt = `Draft a ${docType} with the following details:

${JSON.stringify(details, null, 2)}

Create a complete, legally valid document following Indian legal standards. Include all necessary clauses and provisions.`;

    return await this.chat(prompt, language);
  }

  /**
   * Explain a legal section
   */
  async explainSection(act, section, language = 'english') {
    const prompt = `Explain Section ${section} of ${act} in detail:

1. What does this section state?
2. Essential ingredients/elements
3. Punishment (if applicable)
4. Key exceptions
5. Relevant case law
6. Practical application

Provide a comprehensive explanation suitable for someone without legal background.`;

    return await this.chat(prompt, language);
  }
}

// Create singleton instance
export const offlineLLM = new OfflineLegalLLM();

// Export class for testing
export { OfflineLegalLLM };

// Default export
export default offlineLLM;
