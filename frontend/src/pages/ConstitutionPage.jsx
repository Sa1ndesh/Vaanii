import React, { useState, useEffect, useRef } from 'react';
import { 
  ShieldCheck, 
  Mic, 
  MicOff, 
  Send, 
  Volume2, 
  VolumeX, 
  BookOpen, 
  RefreshCw, 
  AlertCircle, 
  CheckCircle2, 
  Cpu, 
  Sparkles,
  Bookmark
} from 'lucide-react';
import { askConstitution, getConstitutionStatus, transcribeAudio } from '../services/constitutionService';

const SUGGESTED_QUERIES = [
  "What is Article 21?",
  "What are Fundamental Rights?",
  "What is Article 14?",
  "What does the Preamble say?",
  "What is Article 32?",
  "What is the difference between Article 14 and Article 21?",
  "How many Fundamental Duties are there?"
];

function ConstitutionPage() {
  const [question, setQuestion] = useState("");
  const [loading, setLoading] = useState(false);
  const [answerData, setAnswerData] = useState(null);
  const [error, setError] = useState(null);
  
  // Status check state
  const [systemStatus, setSystemStatus] = useState(null);
  const [checkingStatus, setCheckingStatus] = useState(false);

  // Audio Recording (Microphone) State
  const [isRecording, setIsRecording] = useState(false);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);

  // Audio Speaking (TTS) State
  const [isSpeaking, setIsSpeaking] = useState(false);

  // Fetch status on mount
  useEffect(() => {
    fetchStatus();
  }, []);

  const fetchStatus = async () => {
    setCheckingStatus(true);
    try {
      const data = await getConstitutionStatus();
      setSystemStatus(data);
    } catch (err) {
      console.warn("Failed to retrieve system status:", err);
    } finally {
      setCheckingStatus(false);
    }
  };

  const handleAsk = async (queryText = question) => {
    const q = queryText.trim();
    if (!q || loading) return;

    setLoading(true);
    setError(null);
    stopSpeech();

    try {
      const result = await askConstitution(q, 3, "en");
      setAnswerData(result);
    } catch (err) {
      setError(err.message || "Failed to process Constitution query.");
    } finally {
      setLoading(false);
    }
  };

  // --- Voice Input (STT) Handling ---
  const toggleRecording = async () => {
    if (isRecording) {
      // Stop recording
      if (mediaRecorderRef.current && mediaRecorderRef.current.state === "recording") {
        mediaRecorderRef.current.stop();
      }
      setIsRecording(false);
    } else {
      // Start recording
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        audioChunksRef.current = [];
        const mediaRecorder = new MediaRecorder(stream);
        mediaRecorderRef.current = mediaRecorder;

        mediaRecorder.ondataavailable = (event) => {
          if (event.data.size > 0) {
            audioChunksRef.current.push(event.data);
          }
        };

        mediaRecorder.onstop = async () => {
          const audioBlob = new Blob(audioChunksRef.current, { type: "audio/wav" });
          stream.getTracks().forEach((track) => track.stop());
          
          try {
            setLoading(true);
            const transcriptRes = await transcribeAudio(audioBlob);
            if (transcriptRes.text) {
              setQuestion(transcriptRes.text);
              handleAsk(transcriptRes.text);
            }
          } catch (e) {
            setError("Voice transcription failed: " + e.message);
          } finally {
            setLoading(false);
          }
        };

        mediaRecorder.start();
        setIsRecording(true);
      } catch (err) {
        setError("Microphone permission denied or device not found: " + err.message);
      }
    }
  };

  // --- Voice Output (TTS) Handling ---
  const toggleSpeech = () => {
    if (isSpeaking) {
      stopSpeech();
    } else if (answerData?.answer) {
      speakText(answerData.answer);
    }
  };

  const speakText = (text) => {
    if (!('speechSynthesis' in window)) {
      alert("Text-to-speech is not supported by your browser.");
      return;
    }
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.rate = 0.95;
    utterance.onend = () => setIsSpeaking(false);
    utterance.onerror = () => setIsSpeaking(false);
    setIsSpeaking(true);
    window.speechSynthesis.speak(utterance);
  };

  const stopSpeech = () => {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }
    setIsSpeaking(false);
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6 pb-12">
      {/* Top Header Card */}
      <div className="bg-gradient-to-r from-[#0D3048] to-[#184869] text-white rounded-2xl p-6 shadow-xl border border-white/10">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-3">
              <span className="p-2 rounded-xl bg-amber-500/20 text-[#D4AF37] border border-amber-500/30">
                <BookOpen size={24} />
              </span>
              <div>
                <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
                  Constitution of India Assistant
                </h1>
                <p className="text-sm text-gray-300">
                  Local On-Device RAG Intelligence • Zero Cloud Dependency
                </p>
              </div>
            </div>
          </div>

          {/* Prominent OFFLINE MODE Badge */}
          <div className="flex items-center gap-3">
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-emerald-500/20 border border-emerald-500/40 text-emerald-300 shadow-inner">
              <span className="relative flex h-3 w-3">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-3 w-3 bg-emerald-500"></span>
              </span>
              <span className="text-sm font-extrabold tracking-wider">OFFLINE MODE</span>
            </div>
          </div>
        </div>

        {/* Subsystem Health Chips */}
        <div className="mt-6 pt-4 border-t border-white/10 grid grid-cols-2 md:grid-cols-4 gap-3 text-xs">
          <div className="flex items-center gap-2 text-gray-300">
            <CheckCircle2 size={16} className="text-emerald-400 shrink-0" />
            <span>FAISS Vector DB: <b>{systemStatus?.total_indexed_chunks || 56} Chunks</b></span>
          </div>
          <div className="flex items-center gap-2 text-gray-300">
            <Cpu size={16} className="text-emerald-400 shrink-0" />
            <span>Embeddings: <b>all-MiniLM-L6-v2</b></span>
          </div>
          <div className="flex items-center gap-2 text-gray-300">
            <Sparkles size={16} className="text-amber-400 shrink-0" />
            <span>LLM: <b>{systemStatus?.selected_llm_model || "gemma3:4b"}</b></span>
          </div>
          <div className="flex items-center justify-between text-gray-300">
            <span className="flex items-center gap-2">
              <ShieldCheck size={16} className="text-emerald-400 shrink-0" />
              <span>Ollama: <b>{systemStatus?.ollama_available ? "Online" : "Standalone"}</b></span>
            </span>
            <button 
              onClick={fetchStatus} 
              disabled={checkingStatus}
              title="Refresh local engine status"
              className="hover:text-amber-400 transition"
            >
              <RefreshCw size={14} className={checkingStatus ? "animate-spin" : ""} />
            </button>
          </div>
        </div>
      </div>

      {/* Suggested Queries Chips */}
      <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
        <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">
          Suggested Constitutional Inquiries:
        </p>
        <div className="flex flex-wrap gap-2">
          {SUGGESTED_QUERIES.map((q, idx) => (
            <button
              key={idx}
              onClick={() => {
                setQuestion(q);
                handleAsk(q);
              }}
              className="text-xs bg-slate-100 hover:bg-[#0D3048] hover:text-white text-slate-700 font-medium py-1.5 px-3 rounded-lg border border-slate-200 transition-all cursor-pointer"
            >
              {q}
            </button>
          ))}
        </div>
      </div>

      {/* User Input Area */}
      <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm space-y-3">
        <label className="block text-sm font-semibold text-gray-800">
          Ask a Question on Indian Constitutional Law:
        </label>
        <div className="relative">
          <textarea
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleAsk();
              }
            }}
            placeholder="Type your question here (e.g., What is Article 21? Explain the Right to Equality...)"
            rows={3}
            className="w-full p-3.5 pr-28 rounded-xl border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#0D3048] focus:border-transparent text-gray-800 text-sm resize-none"
          />
          <div className="absolute right-3 bottom-3 flex items-center gap-2">
            <button
              type="button"
              onClick={toggleRecording}
              className={`p-2.5 rounded-xl border transition-all ${
                isRecording 
                  ? "bg-red-500 text-white border-red-600 animate-pulse" 
                  : "bg-gray-100 text-gray-700 hover:bg-gray-200 border-gray-300"
              }`}
              title={isRecording ? "Stop voice recording" : "Record voice question"}
            >
              {isRecording ? <MicOff size={18} /> : <Mic size={18} />}
            </button>
            <button
              type="button"
              onClick={() => handleAsk()}
              disabled={loading || !question.trim()}
              className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-[#0D3048] text-[#D4AF37] hover:bg-[#153e5b] font-semibold text-sm transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
            >
              {loading ? (
                <RefreshCw size={16} className="animate-spin" />
              ) : (
                <>
                  <Send size={16} />
                  <span>Ask</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Error Message Area */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-800 p-4 rounded-xl flex items-start gap-3 text-sm">
          <AlertCircle size={20} className="text-red-500 shrink-0 mt-0.5" />
          <div className="space-y-1">
            <p className="font-semibold">Notice</p>
            <p>{error}</p>
          </div>
        </div>
      )}

      {/* Answer Area */}
      {answerData && (
        <div className="bg-white rounded-2xl border border-gray-200 shadow-md p-6 space-y-6">
          <div className="flex items-center justify-between pb-4 border-b border-gray-100">
            <div className="flex items-center gap-2">
              <span className="px-2.5 py-1 rounded-md bg-emerald-100 text-emerald-800 text-xs font-bold uppercase">
                {answerData.offline ? "Offline Verified" : "Online Mode"}
              </span>
              <span className="text-xs text-gray-500">
                Model: <b>{answerData.model_used || "Local RAG"}</b>
              </span>
            </div>

            <button
              onClick={toggleSpeech}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-xs font-semibold transition ${
                isSpeaking 
                  ? "bg-amber-100 text-amber-900 border-amber-300" 
                  : "bg-gray-100 text-gray-700 hover:bg-gray-200 border-gray-300"
              }`}
              title={isSpeaking ? "Mute speech" : "Read answer aloud"}
            >
              {isSpeaking ? (
                <>
                  <VolumeX size={16} />
                  <span>Stop Voice</span>
                </>
              ) : (
                <>
                  <Volume2 size={16} />
                  <span>Speak Answer</span>
                </>
              )}
            </button>
          </div>

          {/* Formatted Answer Text */}
          <div className="prose max-w-none text-gray-800 text-sm leading-relaxed whitespace-pre-line">
            {answerData.answer}
          </div>

          {/* Source Articles Section */}
          {answerData.sources && answerData.sources.length > 0 && (
            <div className="pt-6 border-t border-gray-100 space-y-3">
              <h3 className="text-xs font-bold text-gray-500 uppercase tracking-wider flex items-center gap-1.5">
                <Bookmark size={16} className="text-[#0D3048]" />
                <span>Referenced Constitutional Provisions ({answerData.sources.length}):</span>
              </h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {answerData.sources.map((src, i) => (
                  <div key={i} className="bg-slate-50 border border-slate-200 rounded-xl p-3.5 space-y-1.5">
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-[#0D3048] text-sm">{src.article}</span>
                    </div>
                    <p className="text-xs font-semibold text-gray-700">{src.title}</p>
                    <p className="text-xs text-gray-600 line-clamp-3 leading-relaxed">
                      {src.content}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default ConstitutionPage;
