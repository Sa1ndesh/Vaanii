import React, { useState, useRef, useEffect, useCallback } from 'react';
import { Send, Mic, MicOff, Volume2, Loader2, MapPin, Globe, Sparkles, ArrowLeft, MessageCircle } from 'lucide-react';

import { API_BASE_URL } from '../config';
const API_BASE = `${API_BASE_URL}/api/vani`;

const LANG_META = {
  kannada: {
    label: 'ಕನ್ನಡ', en: 'Kannada', tts: 'kn-IN',
    gradient: 'from-orange-500 to-amber-500', bg: 'bg-orange-50', border: 'border-orange-200', text: 'text-orange-600', ring: 'ring-orange-300',
    welcome: '🙏 ನಮಸ್ಕಾರ! ನಾನು ವಾಣಿ-ಕಾನೂನ್, ನಿಮ್ಮ ಕಾನೂನು ಸಹಾಯಕ.\n\nಪೋಲೀಸ್, ಆಸ್ತಿ, ಹಕ್ಕುಗಳು, ಕುಟುಂಬ ಕಾನೂನು — ಯಾವುದೇ ಕಾನೂನು ಪ್ರಶ್ನೆ ಕೇಳಿ.\nನಾನು ಸರಳವಾಗಿ ವಿವರಿಸುತ್ತೇನೆ!',
    placeholder: 'ನಿಮ್ಮ ಕಾನೂನು ಪ್ರಶ್ನೆ ಇಲ್ಲಿ ಬರೆಯಿರಿ ಅಥವಾ ಮಾತನಾಡಿ...',
    thinking: 'ವಾಣಿ-ಕಾನೂನ್ ಯೋಚಿಸುತ್ತಿದೆ...',
    listen: 'ಕೇಳಿ',
    prompts: ['ಎಫ್‌ಐಆರ್ ಹೇಗೆ ದಾಖಲಿಸುವುದು?', 'ಬಾಡಿಗೆ ಒಪ್ಪಂದ ಹಕ್ಕುಗಳು', 'ಗೃಹ ಹಿಂಸೆ ಸಹಾಯ', 'ಆಸ್ತಿ ವಂಚನೆ ಪ್ರಕರಣ'],
  },
  marathi: {
    label: 'मराठी', en: 'Marathi', tts: 'mr-IN',
    gradient: 'from-purple-500 to-violet-500', bg: 'bg-purple-50', border: 'border-purple-200', text: 'text-purple-600', ring: 'ring-purple-300',
    welcome: '🙏 नमस्कार! मी वाणी-कानून, तुमचा कायदेशीर मित्र.\n\nपोलीस, मालमत्ता, हक्क, कौटुंबिक कायदा — कोणताही कायदेशीर प्रश्न विचारा.\nमी सोप्या भाषेत समजावून सांगतो!',
    placeholder: 'तुमचा कायदेशीर प्रश्न लिहा किंवा बोला...',
    thinking: 'वाणी-कानून विचार करत आहे...',
    listen: 'ऐका',
    prompts: ['एफआयआर कशी दाखल करावी?', 'भाडे करार हक्क', 'घरगुती हिंसाचार मदत', 'मालमत्ता फसवणूक'],
  },
  hindi: {
    label: 'हिंदी', en: 'Hindi', tts: 'hi-IN',
    gradient: 'from-cyan-500 to-blue-500', bg: 'bg-cyan-50', border: 'border-cyan-200', text: 'text-cyan-600', ring: 'ring-cyan-300',
    welcome: '🙏 नमस्ते! मैं वाणी-कानून हूँ, आपका कानूनी सहायक.\n\nपुलिस, संपत्ति, अधिकार, पारिवारिक कानून — कोई भी कानूनी सवाल पूछें.\nमैं आसान भाषा में समझाऊँगा!',
    placeholder: 'अपना कानूनी सवाल यहाँ लिखें या बोलें...',
    thinking: 'वाणी-कानून सोच रहा है...',
    listen: 'सुनें',
    prompts: ['एफआईआर कैसे दर्ज करें?', 'किराया समझौता अधिकार', 'घरेलू हिंसा सहायता', 'संपत्ति धोखाधड़ी मामला'],
  },
  english: {
    label: 'English', en: 'English', tts: 'en-IN',
    gradient: 'from-green-500 to-emerald-500', bg: 'bg-green-50', border: 'border-green-200', text: 'text-green-600', ring: 'ring-green-300',
    welcome: '👋 Welcome! I am Vani-Kanoon, your AI legal assistant for Indian law.\n\nPolice, property, rights, family law — ask any legal question.\nI will explain it in simple, clear English!',
    placeholder: 'Type or speak your legal question here...',
    thinking: 'Vani-Kanoon is thinking...',
    listen: 'Listen',
    prompts: ['How to file an FIR?', 'Tenant rights & rent agreement', 'Domestic violence help', 'Property fraud case'],
  },
};

// ── Speech Recognition hook ──
const useSpeechRecognition = (lang) => {
  const [transcript, setTranscript] = useState('');
  const [listening,  setListening]  = useState(false);
  const recRef = useRef(null);

  const startListening = useCallback(() => {
    const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SR) { alert('Speech Recognition not supported. Use Chrome.'); return; }
    const rec = new SR();
    rec.lang = LANG_META[lang]?.tts || 'hi-IN';
    rec.interimResults = true;
    rec.onresult = (e) => {
      const current = Array.from(e.results)
        .map(result => result[0].transcript)
        .join('');
      setTranscript(current);
    };
    rec.onerror  = () => setListening(false);
    rec.onend    = () => setListening(false);
    recRef.current = rec;
    rec.start();
    setListening(true);
  }, [lang]);

  const stopListening = useCallback(() => {
    recRef.current?.stop();
    setListening(false);
  }, []);

  return { transcript, setTranscript, listening, startListening, stopListening };
};

// ── TTS via backend gTTS (supports real Kannada/Marathi/Hindi) ──
let currentAudio = null;

const speakText = async (text, langCode) => {
  // Stop any currently playing audio
  if (currentAudio) {
    currentAudio.pause();
    currentAudio = null;
  }
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel();
  }

  // Priority 1: High-Quality Microsoft Neural HD Human Voice (Crystal-Clear)
  try {
    const res = await fetch(`${API_BASE}/tts`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text, language: langCode })
    });

    if (res.ok) {
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const audio = new Audio(url);
      currentAudio = audio;
      await audio.play();
      audio.onended = () => { URL.revokeObjectURL(url); currentAudio = null; };
      return;
    }
  } catch (err) {
    console.warn("Neural TTS note, falling back to browser synthesis:", err);
  }

  // Priority 2: Browser Native Synthesis Fallback
  const synth = window.speechSynthesis;
  if (synth) {
    const utter = new SpeechSynthesisUtterance(text);
    const langMap = {
      'hindi': 'hi-IN',
      'kannada': 'kn-IN',
      'marathi': 'mr-IN',
      'english': 'en-IN'
    };
    utter.lang = langMap[langCode] || 'hi-IN';
    utter.rate = 0.9;
    const voices = synth.getVoices();
    const voice = voices.find(v => v.lang.startsWith(utter.lang.split('-')[0]));
    if (voice) utter.voice = voice;
    synth.speak(utter);
  }
};


// ── Chat Bubble ──
function ChatBubble({ msg, langCode }) {
  const isUser = msg.role === 'user';
  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'}`}>
      <div className={`flex items-start max-w-lg gap-3 ${isUser ? 'flex-row-reverse' : ''}`}>
        {/* Avatar */}
        <div className={`flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center text-white shadow-md ${isUser ? 'bg-legal-blue-primary' : 'bg-legal-gold-primary'}`}>
          {isUser ? <MessageCircle size={18} /> : <Sparkles size={18} />}
        </div>
        {/* Bubble */}
        <div className={`px-5 py-4 rounded-2xl shadow-sm ${isUser ? 'bg-legal-blue-primary text-white' : 'bg-legal-gray-bg text-legal-text-primary'}`}>
          <p className="whitespace-pre-wrap text-sm leading-relaxed">{msg.text}</p>

          {/* Dialect tag */}
          {msg.dialect && (
            <div className="mt-3 pt-2 border-t border-legal-gold-primary/40 flex items-center gap-1.5">
              <MapPin size={12} className="text-legal-gold-primary" />
              <span className="text-xs font-semibold text-legal-gold-primary">{msg.dialect}</span>
            </div>
          )}

          {/* RAG sources */}
          {msg.docs?.length > 0 && (
            <div className="mt-2 flex flex-wrap gap-1">
              {msg.docs.map(d => (
                <span key={d.id} className="inline-block px-2 py-0.5 rounded-full text-[10px] bg-legal-blue-primary/10 text-legal-blue-primary font-medium">
                  📖 {d.title}
                </span>
              ))}
            </div>
          )}

          {/* Listen button */}
          {!isUser && (
            <button onClick={() => speakText(msg.text, langCode)}
              className="mt-2 flex items-center gap-1 text-xs text-legal-blue-highlight hover:text-legal-blue-primary transition-colors">
              <Volume2 size={14} /> {LANG_META[langCode]?.listen || 'Listen'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

// ── Main Page ──
export default function VaniKanoonPage() {
  const [step,        setStep]        = useState('language');
  const [language,    setLanguage]    = useState('');
  const [stateName,   setStateName]   = useState('');
  const [states,      setStates]      = useState([]);
  const [district,    setDistrict]    = useState('');
  const [districts,   setDistricts]   = useState([]);
  const [dialectInfo, setDialectInfo] = useState(null);
  const [messages,    setMessages]    = useState([]);
  const [input,       setInput]       = useState('');
  const [loading,     setLoading]     = useState(false);
  const bottomRef = useRef(null);

  const { transcript, setTranscript, listening, startListening, stopListening } =
    useSpeechRecognition(language);

  useEffect(() => { if (transcript) setInput(transcript); }, [transcript]);
  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  const selectLanguage = async (lang) => {
    setLanguage(lang);
    setStep('state');
    try {
      const res = await fetch(`${API_BASE}/states/${lang}`);
      const data = await res.json();
      setStates(data.states || []);
    } catch { setStates([]); }
  };

  const selectState = async (st) => {
    setStateName(st);
    setStep('district');
    try {
      const res = await fetch(`${API_BASE}/districts/${language}?state=${encodeURIComponent(st)}`);
      const data = await res.json();
      setDistricts(data.districts || []);
    } catch { setDistricts([]); }
  };

  const selectDistrict = async (dist) => {
    setDistrict(dist);
    try {
      const res = await fetch(`${API_BASE}/dialect-info/${language}/${dist}`);
      const data = await res.json();
      setDialectInfo(data);
    } catch { setDialectInfo(null); }
    setMessages([{
      role: 'bot',
      text: LANG_META[language]?.welcome || `🙏 Welcome! Ask any legal question.`,
      dialect: null, docs: []
    }]);
    setStep('chat');
  };

  const sendMessage = async (text) => {
    if (!text.trim() || loading) return;
    setMessages(prev => [...prev, { role: 'user', text: text.trim() }]);
    setInput(''); setTranscript('');
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/ask`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ language, district, query: text.trim() })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.detail || 'Error');
      setMessages(prev => [...prev, {
        role: 'bot', text: data.answer,
        dialect: data.dialect, docs: data.relevant_docs || []
      }]);
    } catch (e) {
      setMessages(prev => [...prev, { role: 'bot', text: `❌ ${e.message}`, dialect: null, docs: [] }]);
    }
    setLoading(false);
  };

  const meta = LANG_META[language] || {};

  return (
    <div className="flex flex-col h-[calc(100vh-10rem)] max-w-4xl mx-auto">

      {/* ═══════════════ STEP 1 — LANGUAGE ═══════════════ */}
      {step === 'language' && (
        <div className="flex-1 flex flex-col items-center justify-center bg-white rounded-xl shadow-md p-8">
          <div className="text-center mb-10">
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-legal-blue-primary to-legal-blue-highlight flex items-center justify-center mx-auto mb-4 shadow-lg">
              <Mic size={32} className="text-white" />
            </div>
            <h2 className="text-2xl font-bold text-legal-text-primary">Welcome to Vani-Kanoon</h2>
            <p className="text-sm text-gray-500 mt-2">Your multilingual voice-based legal assistant</p>
            <p className="text-sm text-legal-gold-primary font-medium mt-1">बोलिए, हम सुनेंगे</p>
          </div>

          <h3 className="text-base font-semibold text-legal-text-primary mb-6 flex items-center gap-2">
            <Globe size={18} className="text-legal-blue-highlight" />
            Select Your Language
          </h3>

          <div className="grid grid-cols-3 gap-5 w-full max-w-md">
            {Object.entries(LANG_META).map(([code, m]) => (
              <button key={code} onClick={() => selectLanguage(code)}
                className={`group relative p-6 rounded-2xl border-2 ${m.border} ${m.bg} hover:shadow-lg hover:-translate-y-1 transition-all duration-200 cursor-pointer`}>
                <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${m.gradient} flex items-center justify-center mx-auto mb-3 shadow-md group-hover:scale-110 transition-transform`}>
                  <Globe size={22} className="text-white" />
                </div>
                <p className={`text-xl font-bold text-center ${m.text}`}>{m.label}</p>
                <p className="text-xs text-center text-gray-400 mt-1">{m.en}</p>
              </button>
            ))}
          </div>
        </div>
      )}

      {/* ═══════════════ STEP 2 — STATE ═══════════════ */}
      {step === 'state' && (
        <div className="flex-1 flex flex-col bg-white rounded-xl shadow-md p-6 overflow-hidden">
          <div className="text-center mb-5">
            <div className={`inline-flex items-center gap-2 px-4 py-1.5 rounded-full ${meta.bg} ${meta.border} border mb-3`}>
              <Globe size={14} className={meta.text} />
              <span className={`text-sm font-semibold ${meta.text}`}>{meta.en}</span>
            </div>
            <h3 className="text-lg font-bold text-legal-text-primary flex items-center justify-center gap-2">
              <MapPin size={18} className="text-legal-gold-primary" />
              Select Your State
            </h3>
            <p className="text-xs text-gray-400 mt-1">Select your state to locate your region</p>
          </div>

          <div className="flex-1 overflow-y-auto grid grid-cols-2 sm:grid-cols-3 gap-3 content-start">
            {states.map(st => (
              <button key={st} onClick={() => selectState(st)}
                className={`group p-4 rounded-xl border ${meta.border} hover:${meta.bg} hover:shadow-md transition-all text-left cursor-pointer flex items-center justify-between`}>
                <div className="flex items-center gap-2">
                  <MapPin size={16} className={`${meta.text} opacity-60 group-hover:opacity-100 transition-opacity`} />
                  <span className="text-sm text-legal-text-primary font-semibold">{st}</span>
                </div>
              </button>
            ))}
          </div>

          <button onClick={() => { setStep('language'); setLanguage(''); }}
            className="mt-4 flex items-center justify-center gap-2 py-2.5 rounded-lg border border-gray-200 text-sm text-gray-500 hover:bg-gray-50 transition-colors cursor-pointer">
            <ArrowLeft size={16} /> Change Language
          </button>
        </div>
      )}

      {/* ═══════════════ STEP 3 — DISTRICT ═══════════════ */}
      {step === 'district' && (
        <div className="flex-1 flex flex-col bg-white rounded-xl shadow-md p-6 overflow-hidden">
          <div className="text-center mb-5">
            <div className="flex items-center justify-center gap-2 flex-wrap mb-3">
              <div className={`inline-flex items-center gap-2 px-4 py-1 rounded-full ${meta.bg} ${meta.border} border`}>
                <Globe size={14} className={meta.text} />
                <span className={`text-sm font-semibold ${meta.text}`}>{meta.en}</span>
              </div>
              <div className="inline-flex items-center gap-2 px-4 py-1 rounded-full bg-amber-50 border border-amber-200">
                <MapPin size={14} className="text-amber-600" />
                <span className="text-sm font-semibold text-amber-600">{stateName}</span>
              </div>
            </div>
            <h3 className="text-lg font-bold text-legal-text-primary flex items-center justify-center gap-2">
              <MapPin size={18} className="text-legal-gold-primary" />
              Select Your District
            </h3>
            <p className="text-xs text-gray-400 mt-1">We'll match your local dialect for a natural conversation</p>
          </div>

          <div className="flex-1 overflow-y-auto grid grid-cols-2 sm:grid-cols-3 gap-2 content-start">
            {districts.map(d => (
              <button key={d} onClick={() => selectDistrict(d)}
                className={`group p-3 rounded-xl border ${meta.border} hover:${meta.bg} hover:shadow-md transition-all text-left cursor-pointer`}>
                <div className="flex items-center gap-2">
                  <MapPin size={14} className={`${meta.text} opacity-50 group-hover:opacity-100 transition-opacity`} />
                  <span className="text-sm text-legal-text-primary font-medium">{d}</span>
                </div>
              </button>
            ))}
          </div>

          <button onClick={() => { setStep('state'); }}
            className="mt-4 flex items-center justify-center gap-2 py-2.5 rounded-lg border border-gray-200 text-sm text-gray-500 hover:bg-gray-50 transition-colors cursor-pointer">
            <ArrowLeft size={16} /> Back to State
          </button>
        </div>
      )}

      {/* ═══════════════ STEP 4 — CHAT ═══════════════ */}
      {step === 'chat' && (
        <>
          {/* Context chips */}
          <div className="flex items-center gap-2 mb-3 flex-wrap">
            <div className={`flex items-center gap-1.5 px-3 py-1 rounded-full border ${meta.border} ${meta.bg}`}>
              <Globe size={12} className={meta.text} />
              <span className={`text-xs font-semibold ${meta.text}`}>{meta.en}</span>
            </div>
            {stateName && (
              <div className="flex items-center gap-1.5 px-3 py-1 rounded-full border border-blue-200 bg-blue-50">
                <MapPin size={12} className="text-blue-600" />
                <span className="text-xs font-semibold text-blue-600">{stateName}</span>
              </div>
            )}
            <div className="flex items-center gap-1.5 px-3 py-1 rounded-full border border-amber-200 bg-amber-50">
              <MapPin size={12} className="text-amber-600" />
              <span className="text-xs font-semibold text-amber-600">{district}</span>
            </div>
            {dialectInfo && (
              <div className="flex items-center gap-1.5 px-3 py-1 rounded-full border border-legal-gold-primary/40 bg-yellow-50">
                <Sparkles size={12} className="text-legal-gold-primary" />
                <span className="text-xs font-semibold text-legal-gold-hover">{dialectInfo.dialect}</span>
              </div>
            )}
            <button onClick={() => { setStep('language'); setLanguage(''); setStateName(''); setDistrict(''); setMessages([]); }}
              className="ml-auto flex items-center gap-1 px-3 py-1 rounded-full text-xs text-gray-400 border border-gray-200 hover:bg-gray-50 transition-colors cursor-pointer">
              <ArrowLeft size={12} /> Change
            </button>
          </div>

          {/* Message history */}
          <div className="flex-1 overflow-y-auto p-6 space-y-6 bg-white rounded-t-lg shadow-md">
            {messages.map((msg, i) => <ChatBubble key={i} msg={msg} langCode={language} />)}

            {loading && (
              <div className="flex justify-start">
                <div className="flex items-start gap-3">
                  <div className="flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center bg-legal-gold-primary text-white shadow-md">
                    <Loader2 size={20} className="animate-spin" />
                  </div>
                  <div className="px-5 py-4 rounded-2xl bg-legal-gray-bg text-legal-text-primary">
                    <p className="italic text-sm">{meta.thinking || 'Thinking...'}</p>
                  </div>
                </div>
              </div>
            )}
            <div ref={bottomRef} />
          </div>

          {/* Quick prompts */}
          {messages.length <= 1 && (
            <div className="flex gap-2 flex-wrap px-4 py-2 bg-white border-x border-gray-100">
              {(meta.prompts || ['How to file FIR?', 'Rent agreement rights', 'Domestic violence help', 'Property cheating']).map(q => (
                <button key={q} onClick={() => sendMessage(q)}
                  className="px-3 py-1.5 rounded-full text-xs bg-legal-blue-primary/5 border border-legal-blue-primary/20 text-legal-blue-primary hover:bg-legal-blue-primary/10 transition-colors cursor-pointer">
                  {q}
                </button>
              ))}
            </div>
          )}

          {/* Input bar */}
          <div className="p-4 bg-white rounded-b-lg shadow-md border-t border-gray-200">
            <div className="flex items-center gap-3">
              {/* Mic button */}
              <button onClick={listening ? stopListening : startListening}
                className={`p-3 rounded-lg text-white transition-all shadow-md ${listening
                  ? 'bg-red-500 hover:bg-red-600 animate-pulse'
                  : 'bg-legal-gold-primary hover:bg-legal-gold-hover'
                }`}
                title={listening ? 'Stop recording' : 'Speak your question'}>
                {listening ? <MicOff size={22} /> : <Mic size={22} />}
              </button>

              {/* Text input */}
              <input
                type="text"
                value={input}
                onChange={e => setInput(e.target.value)}
                onKeyDown={e => { if (e.key === 'Enter') { e.preventDefault(); sendMessage(input); }}}
                placeholder={meta.placeholder || `Type or speak in ${meta.en || 'your language'}...`}
                className={`flex-1 rounded-lg border border-gray-300 p-3.5 focus:outline-none focus:ring-2 focus:${meta.ring || 'ring-legal-blue-primary'}`}
                disabled={loading}
              />

              {/* Send button */}
              <button onClick={() => sendMessage(input)} disabled={!input.trim() || loading}
                className={`p-3.5 rounded-lg text-white transition-colors shadow-md ${loading || !input.trim() ? 'bg-gray-400' : 'bg-legal-blue-primary hover:bg-legal-blue-highlight'}`}>
                {loading ? <Loader2 size={22} className="animate-spin" /> : <Send size={22} />}
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
