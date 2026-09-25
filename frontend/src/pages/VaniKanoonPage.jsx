import React, { useState, useRef, useEffect, useCallback } from 'react';
import { Send, Mic, MicOff, Volume2, Square, Loader2, Globe, Sparkles, MessageCircle, Wifi, WifiOff } from 'lucide-react';
import { getOfflineLegalAnswer } from '../services/offlineLegalKB';
import geminiDirect from '../services/geminiDirect';

const LANG_META = {
  kannada: {
    label: 'ಕನ್ನಡ', en: 'Kannada', tts: 'kn-IN',
    gradient: 'from-orange-500 to-amber-500', bg: 'bg-orange-50', border: 'border-orange-200', text: 'text-orange-600',
    welcome: '🙏 ನಮಸ್ಕಾರ! ನಾನು ವಾಣಿ-ಕಾನೂನ್, ನಿಮ್ಮ ಕಾನೂನು ಸಹಾಯಕ.\n\n✅ ಆಫ್‌ಲೈನ್ ಮೋಡ್ ಲಭ್ಯವಿದೆ\n\nFIR, ಜಾಮೀನು, ಆಸ್ತಿ, ವಿಚ್ಛೇದನ — ಯಾವುದೇ ಕಾನೂನು ಪ್ರಶ್ನೆ ಕೇಳಿ.',
    placeholder: 'ನಿಮ್ಮ ಕಾನೂನು ಪ್ರಶ್ನೆ ಇಲ್ಲಿ ಬರೆಯಿರಿ...',
    thinking: 'ಹುಡುಕುತ್ತಿದೆ...',
    listen: 'ಕೇಳಿ', stop: 'ನಿಲ್ಲಿಸಿ',
    prompts: ['FIR ಹೇಗೆ ದಾಖಲಿಸುವುದು?', 'ಜಾಮೀನು ಹೇಗೆ ಪಡೆಯುವುದು?', 'ಗೃಹ ಹಿಂಸೆ ಕಾನೂನು'],
  },
  marathi: {
    label: 'मराठी', en: 'Marathi', tts: 'mr-IN',
    gradient: 'from-purple-500 to-violet-500', bg: 'bg-purple-50', border: 'border-purple-200', text: 'text-purple-600',
    welcome: '🙏 नमस्कार! मी वाणी-कानून, तुमचा कायदेशीर मित्र.\n\n✅ ऑफलाइन मोड उपलब्ध\n\nFIR, जामीन, मालमत्ता, घटस्फोट — कोणताही प्रश्न विचारा.',
    placeholder: 'तुमचा कायदेशीर प्रश्न लिहा...',
    thinking: 'शोधत आहे...',
    listen: 'ऐका', stop: 'थांबवा',
    prompts: ['FIR कशी दाखल करावी?', 'जामीन कसा मिळवावा?', 'घरगुती हिंसाचार कायदा'],
  },
  hindi: {
    label: 'हिंदी', en: 'Hindi', tts: 'hi-IN',
    gradient: 'from-cyan-500 to-blue-500', bg: 'bg-cyan-50', border: 'border-cyan-200', text: 'text-cyan-600',
    welcome: '🙏 नमस्ते! मैं वाणी-कानून हूँ, आपका कानूनी सहायक.\n\n✅ ऑफलाइन मोड उपलब्ध\n\nFIR, जमानत, संपत्ति, तलाक — कोई भी सवाल पूछें.',
    placeholder: 'अपना कानूनी सवाल यहाँ लिखें...',
    thinking: 'खोज रहा है...',
    listen: 'सुनें', stop: 'रोकें',
    prompts: ['FIR कैसे दर्ज करें?', 'जमानत कैसे मिलती है?', 'घरेलू हिंसा कानून'],
  },
  english: {
    label: 'English', en: 'English', tts: 'en-IN',
    gradient: 'from-green-500 to-emerald-500', bg: 'bg-green-50', border: 'border-green-200', text: 'text-green-600',
    welcome: '👋 Welcome! I am Vani-Kanoon, your legal assistant.\n\n✅ Offline mode available\n\nFIR, bail, property, divorce — ask any legal question.',
    placeholder: 'Type your legal question here...',
    thinking: 'Searching...',
    listen: 'Listen', stop: 'Stop',
    prompts: ['How to file FIR?', 'How to get bail?', 'Domestic violence laws'],
  },
};

// Speech Recognition hook
const useSpeechRecognition = (lang) => {
  const [transcript, setTranscript] = useState('');
  const [listening, setListening] = useState(false);
  const recRef = useRef(null);

  const startListening = useCallback(() => {
    const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SR) { alert('Speech Recognition not supported'); return; }
    const rec = new SR();
    rec.lang = LANG_META[lang]?.tts || 'hi-IN';
    rec.interimResults = true;
    rec.onresult = (e) => {
      const current = Array.from(e.results).map(r => r[0].transcript).join('');
      setTranscript(current);
    };
    rec.onerror = () => setListening(false);
    rec.onend = () => setListening(false);
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

// Browser TTS
const speakText = (text, langCode) => {
  if (!('speechSynthesis' in window)) return null;
  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(text.replace(/\*\*/g, '').replace(/[#\-|]/g, ' '));
  utterance.lang = LANG_META[langCode]?.tts || 'en-IN';
  utterance.rate = 0.9;
  return utterance;
};

// Chat Bubble
function ChatBubble({ msg, langCode, isPlaying, onPlay, onStop }) {
  const isUser = msg.role === 'user';
  const meta = LANG_META[langCode] || {};

  return (
    <div className={`flex ${isUser ? 'justify-end' : 'justify-start'}`}>
      <div className={`flex items-start max-w-lg gap-3 ${isUser ? 'flex-row-reverse' : ''}`}>
        <div className={`flex-shrink-0 w-10 h-10 rounded-full flex items-center justify-center text-white shadow-md ${isUser ? 'bg-legal-blue-primary' : 'bg-legal-gold-primary'}`}>
          {isUser ? <MessageCircle size={18} /> : <Sparkles size={18} />}
        </div>
        <div className={`px-5 py-4 rounded-2xl shadow-sm ${isUser ? 'bg-legal-blue-primary text-white' : 'bg-legal-gray-bg text-legal-text-primary'}`}>
          <p className="whitespace-pre-wrap text-sm leading-relaxed">{msg.text}</p>
          {msg.source && (
            <div className="mt-2 flex items-center gap-1 text-xs text-gray-500">
              {msg.source === 'offline' ? <WifiOff size={12} /> : <Wifi size={12} />}
              {msg.source === 'offline' ? 'Offline KB' : 'AI Response'}
            </div>
          )}
          {!isUser && (
            <div className="mt-3 pt-2 border-t border-gray-100 flex items-center">
              {isPlaying ? (
                <button onClick={onStop}
                  className="flex items-center gap-1.5 text-xs font-medium px-2.5 py-1 rounded-full bg-red-50 text-red-600 border border-red-200 hover:bg-red-100 transition-colors cursor-pointer">
                  <Square size={12} className="fill-current" /> {meta.stop || 'Stop'}
                </button>
              ) : (
                <button onClick={() => onPlay(msg)}
                  className="flex items-center gap-1.5 text-xs font-medium text-legal-blue-highlight hover:text-legal-blue-primary transition-colors cursor-pointer">
                  <Volume2 size={15} /> {meta.listen || 'Listen'}
                </button>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// Main Page
export default function VaniKanoonPage() {
  const [step, setStep] = useState('language');
  const [language, setLanguage] = useState('');
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [playingMsgId, setPlayingMsgId] = useState(null);
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const bottomRef = useRef(null);

  const { transcript, setTranscript, listening, startListening, stopListening } = useSpeechRecognition(language);

  useEffect(() => { if (transcript) setInput(transcript); }, [transcript]);
  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  // Monitor online status
  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  const stopAudio = useCallback(() => {
    window.speechSynthesis.cancel();
    setPlayingMsgId(null);
  }, []);

  useEffect(() => () => window.speechSynthesis.cancel(), []);

  const playAudio = useCallback((msg) => {
    if (playingMsgId === msg._id) { stopAudio(); return; }
    stopAudio();
    const utterance = speakText(msg.text, language);
    if (utterance) {
      setPlayingMsgId(msg._id);
      utterance.onend = () => setPlayingMsgId(null);
      utterance.onerror = () => setPlayingMsgId(null);
      window.speechSynthesis.speak(utterance);
    }
  }, [playingMsgId, stopAudio, language]);

  const selectLanguage = (lang) => {
    stopAudio();
    setLanguage(lang);
    geminiDirect.clearHistory();
    const welcomeText = LANG_META[lang]?.welcome || '👋 Welcome!';
    setMessages([{ _id: `welcome_${Date.now()}`, role: 'bot', text: welcomeText, source: 'offline' }]);
    setStep('chat');
  };

  const sendMessage = async (text) => {
    if (!text.trim() || loading) return;
    setMessages(prev => [...prev, { role: 'user', text: text.trim(), _id: `user_${Date.now()}` }]);
    setInput(''); setTranscript('');
    setLoading(true);

    try {
      // STEP 1: Try offline knowledge base first
      const offlineResult = getOfflineLegalAnswer(text.trim(), language);

      if (offlineResult.source !== 'no_match') {
        // Good offline answer found
        setMessages(prev => [...prev, {
          _id: `bot_${Date.now()}`,
          role: 'bot',
          text: offlineResult.answer,
          source: 'offline'
        }]);
      } else if (isOnline) {
        // STEP 2: If online and no offline match, use Gemini
        const geminiResult = await geminiDirect.chat(text.trim(), language);
        if (geminiResult.success) {
          setMessages(prev => [...prev, {
            _id: `bot_${Date.now()}`,
            role: 'bot',
            text: geminiResult.response,
            source: 'online'
          }]);
        } else {
          throw new Error(geminiResult.error);
        }
      } else {
        // Offline and no match
        setMessages(prev => [...prev, {
          _id: `bot_${Date.now()}`,
          role: 'bot',
          text: offlineResult.answer,
          source: 'offline'
        }]);
      }
    } catch (e) {
      setMessages(prev => [...prev, {
        _id: `bot_err_${Date.now()}`,
        role: 'bot',
        text: `❌ Error: ${e.message}`,
        source: 'error'
      }]);
    }
    setLoading(false);
  };

  const meta = LANG_META[language] || {};

  return (
    <div className="flex flex-col h-[calc(100vh-10rem)] max-w-4xl mx-auto">

      {/* LANGUAGE SELECTION */}
      {step === 'language' && (
        <div className="flex-1 flex flex-col items-center justify-center bg-white rounded-xl shadow-md p-8">
          <div className="text-center mb-10">
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-legal-blue-primary to-legal-blue-highlight flex items-center justify-center mx-auto mb-4 shadow-lg">
              <Mic size={32} className="text-white" />
            </div>
            <h2 className="text-2xl font-bold text-legal-text-primary">Vani-Kanoon</h2>
            <p className="text-sm text-gray-500 mt-2">Your Legal Assistant</p>
            <div className="flex items-center justify-center gap-2 mt-2">
              <div className={`w-2 h-2 rounded-full ${isOnline ? 'bg-green-500' : 'bg-orange-500'}`}></div>
              <p className="text-xs text-gray-500">
                {isOnline ? '✅ Online + Offline mode' : '📴 Offline mode active'}
              </p>
            </div>
          </div>

          <h3 className="text-base font-semibold text-legal-text-primary mb-6 flex items-center gap-2">
            <Globe size={18} className="text-legal-blue-highlight" /> Select Language
          </h3>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 w-full max-w-lg">
            {Object.entries(LANG_META).map(([code, m]) => (
              <button key={code} onClick={() => selectLanguage(code)}
                className={`group p-5 rounded-2xl border-2 ${m.border} ${m.bg} hover:shadow-lg hover:-translate-y-1 transition-all duration-200 cursor-pointer`}>
                <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${m.gradient} flex items-center justify-center mx-auto mb-2 shadow-md group-hover:scale-110 transition-transform`}>
                  <Globe size={20} className="text-white" />
                </div>
                <p className={`text-lg font-bold text-center ${m.text}`}>{m.label}</p>
                <p className="text-xs text-center text-gray-400">{m.en}</p>
              </button>
            ))}
          </div>
        </div>
      )}

      {/* CHAT */}
      {step === 'chat' && (
        <>
          {/* Header */}
          <div className={`flex items-center justify-between px-5 py-3 rounded-t-xl bg-gradient-to-r ${meta.gradient} text-white shadow-md`}>
            <div className="flex items-center gap-3">
              <button onClick={() => { stopAudio(); setStep('language'); }}
                className="p-1.5 rounded-full bg-white/20 hover:bg-white/30 transition-colors cursor-pointer">
                <Globe size={18} />
              </button>
              <div>
                <h2 className="font-bold text-lg">Vani-Kanoon</h2>
                <p className="text-xs opacity-90">{meta.en}</p>
              </div>
            </div>
            <div className={`flex items-center gap-2 text-xs px-3 py-1 rounded-full ${isOnline ? 'bg-green-500/30' : 'bg-orange-500/30'}`}>
              {isOnline ? <Wifi size={14} /> : <WifiOff size={14} />}
              {isOnline ? 'Online' : 'Offline'}
            </div>
          </div>

          {/* Messages */}
          <div className="flex-1 overflow-y-auto bg-white px-4 py-5 space-y-5">
            {messages.map(msg => (
              <ChatBubble key={msg._id} msg={msg} langCode={language}
                isPlaying={playingMsgId === msg._id}
                onPlay={playAudio} onStop={stopAudio} />
            ))}
            {loading && (
              <div className="flex items-center gap-2 text-legal-blue-highlight text-sm pl-14">
                <Loader2 size={18} className="animate-spin" /> {meta.thinking || 'Searching...'}
              </div>
            )}
            <div ref={bottomRef} />
          </div>

          {/* Quick Prompts */}
          <div className="bg-gray-50 px-4 py-2 border-t flex gap-2 overflow-x-auto">
            {meta.prompts?.map((p, i) => (
              <button key={i} onClick={() => sendMessage(p)}
                className="whitespace-nowrap px-3 py-1.5 rounded-full text-xs font-medium bg-white border border-gray-200 hover:border-legal-blue-primary hover:text-legal-blue-primary transition-colors cursor-pointer">
                {p}
              </button>
            ))}
          </div>

          {/* Input */}
          <div className="bg-white border-t px-4 py-3 rounded-b-xl shadow-inner">
            <div className="flex items-center gap-2">
              <button onClick={listening ? stopListening : startListening}
                className={`p-3 rounded-full transition-all cursor-pointer ${listening ? 'bg-red-500 text-white animate-pulse' : 'bg-legal-blue-primary/10 text-legal-blue-primary hover:bg-legal-blue-primary/20'}`}>
                {listening ? <MicOff size={20} /> : <Mic size={20} />}
              </button>
              <input type="text" value={input} onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && sendMessage(input)}
                placeholder={meta.placeholder || 'Type your question...'}
                className="flex-1 px-4 py-3 rounded-xl border border-gray-200 focus:border-legal-blue-primary focus:ring-2 focus:ring-legal-blue-primary/20 outline-none text-sm"
                disabled={loading} />
              <button onClick={() => sendMessage(input)} disabled={loading || !input.trim()}
                className="p-3 rounded-full bg-legal-blue-primary text-white hover:bg-legal-blue-highlight disabled:opacity-50 disabled:cursor-not-allowed transition-colors cursor-pointer">
                <Send size={20} />
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
