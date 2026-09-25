/**
 * Direct Gemini API Service - No backend required
 * Calls Gemini API directly from the frontend
 */

const GEMINI_API_KEY = 'AIzaSyCm7jTwiHnPyiNkXRdT3kXUJbNOe5ONJAI';
const GEMINI_API_URL = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent';

const LEGAL_SYSTEM_PROMPT = `You are Vani Kanoon, an expert Indian legal assistant specializing in:
- Bharatiya Nyaya Sanhita (BNS) 2023 - replaced IPC
- Bharatiya Nagarik Suraksha Sanhita (BNSS) 2023 - replaced CrPC
- Bharatiya Sakshya Adhiniyam (BSA) 2023 - replaced Evidence Act
- Indian Penal Code (IPC) 1860 (historical reference)
- Code of Criminal Procedure (CrPC) 1973 (historical reference)
- Indian Evidence Act 1872 (historical reference)
- Constitutional Law and Fundamental Rights
- Family Law, Property Law, Consumer Protection
- Motor Vehicles Act, Cyber Laws, Labor Laws

IMPORTANT GUIDELINES:
1. Always cite specific sections with their source (BNS/IPC/BNSS/CrPC etc.)
2. When relevant, mention both old law (IPC) and new law (BNS) equivalents
3. Provide practical steps citizens can take
4. Include relevant case law when applicable
5. Always add disclaimer about consulting a lawyer for specific cases
6. Be helpful to common citizens who may not understand legal jargon

Format responses clearly with:
- Relevant Law/Section
- Explanation in simple terms
- Practical steps
- Important notes`;

class GeminiDirectService {
  constructor() {
    this.conversationHistory = [];
  }

  async chat(query, language = 'english') {
    const languageInstruction = language !== 'english'
      ? `\n\nIMPORTANT: Respond in ${language} language.`
      : '';

    const requestBody = {
      contents: [
        {
          role: 'user',
          parts: [{ text: LEGAL_SYSTEM_PROMPT + languageInstruction }]
        },
        {
          role: 'model',
          parts: [{ text: 'I am Vani Kanoon, your Indian legal assistant. I will help you understand Indian laws including the new BNS, BNSS, and BSA codes, as well as IPC and other legislation. How can I assist you today?' }]
        },
        ...this.conversationHistory,
        {
          role: 'user',
          parts: [{ text: query }]
        }
      ],
      generationConfig: {
        temperature: 0.7,
        topK: 40,
        topP: 0.95,
        maxOutputTokens: 2048,
      },
      safetySettings: [
        { category: 'HARM_CATEGORY_HARASSMENT', threshold: 'BLOCK_NONE' },
        { category: 'HARM_CATEGORY_HATE_SPEECH', threshold: 'BLOCK_NONE' },
        { category: 'HARM_CATEGORY_SEXUALLY_EXPLICIT', threshold: 'BLOCK_NONE' },
        { category: 'HARM_CATEGORY_DANGEROUS_CONTENT', threshold: 'BLOCK_NONE' }
      ]
    };

    try {
      const response = await fetch(`${GEMINI_API_URL}?key=${GEMINI_API_KEY}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody)
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error?.message || 'Gemini API failed');
      }

      const data = await response.json();
      const assistantResponse = data.candidates?.[0]?.content?.parts?.[0]?.text || 'No response generated';

      // Update conversation history
      this.conversationHistory.push(
        { role: 'user', parts: [{ text: query }] },
        { role: 'model', parts: [{ text: assistantResponse }] }
      );

      // Keep only last 10 exchanges
      if (this.conversationHistory.length > 20) {
        this.conversationHistory = this.conversationHistory.slice(-20);
      }

      return {
        success: true,
        response: assistantResponse,
        mode: 'gemini-direct'
      };
    } catch (error) {
      console.error('Gemini API error:', error);
      return {
        success: false,
        error: error.message,
        mode: 'error'
      };
    }
  }

  async analyzeFIR(text, language = 'english') {
    const prompt = `Analyze this FIR (First Information Report) and provide:
1. Summary of allegations
2. Relevant sections of law (BNS/IPC)
3. Potential defenses
4. Next steps for the accused
5. Important timelines and bail provisions

FIR Content:
${text}

${language !== 'english' ? `Respond in ${language} language.` : ''}`;

    return this.chat(prompt, language);
  }

  async summarizeCase(text, language = 'english') {
    const prompt = `Summarize this legal case/document and provide:
1. Brief facts
2. Key legal issues
3. Applicable laws
4. Important observations
5. Practical implications

Document:
${text}

${language !== 'english' ? `Respond in ${language} language.` : ''}`;

    return this.chat(prompt, language);
  }

  clearHistory() {
    this.conversationHistory = [];
  }
}

export const geminiDirect = new GeminiDirectService();
export default geminiDirect;
