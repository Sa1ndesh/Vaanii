"""
Offline Voice Pipeline Endpoints for Vani-Kanoon.
Supports:
- POST /api/voice/transcribe (Audio file + Optional language -> Text via faster-whisper)
- POST /api/voice/synthesize (Text -> Audio file via Piper/pyttsx3)
- POST /api/voice/ask (Audio file + Optional language -> STT -> Constitution RAG -> TTS audio answer)
Does NOT require Gemini API or Internet connectivity.
"""

import os
import uuid
import tempfile
import logging
from pathlib import Path
from typing import Optional
from fastapi import APIRouter, UploadFile, File, Form, Query, HTTPException
from fastapi.responses import FileResponse
from pydantic import BaseModel

from speech.stt import get_stt_engine
from speech.tts import get_tts_engine
from rag.retriever import get_constitution_retriever
from rag.generator import get_generator

logger = logging.getLogger("vanikanoon.voice_api")

router = APIRouter(prefix="/api/voice", tags=["Offline Voice Processing"])

TEMP_AUDIO_DIR = Path(__file__).resolve().parent.parent / "data" / "audio"
TEMP_AUDIO_DIR.mkdir(parents=True, exist_ok=True)


class SynthesizeRequest(BaseModel):
    text: str
    language: Optional[str] = None


@router.post("/transcribe")
async def transcribe_audio(
    audio: UploadFile = File(...),
    language: Optional[str] = Form(None),
    lang: Optional[str] = Query(None)
):
    """
    Transcribes uploaded audio buffer completely offline using faster-whisper with resilient fallback.
    Accepts audio file and optional language code (e.g. 'kn-IN', 'Kannada', 'mr-IN', 'Marathi', 'hi-IN').
    """
    if not audio.filename:
        raise HTTPException(status_code=400, detail="Audio file must have a filename.")

    chosen_language = language or lang
    suffix = Path(audio.filename).suffix or ".wav"
    temp_file = TEMP_AUDIO_DIR / f"upload_{uuid.uuid4().hex}{suffix}"

    try:
        contents = await audio.read()
        temp_file.write_bytes(contents)

        stt = get_stt_engine()
        result = stt.transcribe_with_info(str(temp_file), language=chosen_language)
        return {
            "text": result.get("text", ""),
            "language": result.get("language"),
            "language_probability": result.get("language_probability"),
            "duration": result.get("duration"),
            "latency_seconds": result.get("latency_seconds"),
            "engine": result.get("engine", "faster-whisper"),
            "stage": result.get("stage", 1),
            "compute_type": result.get("compute_type") or result.get("active_compute_type"),
            "device": result.get("device") or result.get("active_device"),
            "reinitialized": result.get("reinitialized", False),
            "offline": True
        }
    except Exception as e:
        logger.error(f"Transcribe endpoint error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"Speech-to-text failed: {str(e)}")
    finally:
        if temp_file.exists():
            try:
                temp_file.unlink()
            except Exception:
                pass


@router.post("/synthesize")
async def synthesize_speech(request: SynthesizeRequest):
    """
    Synthesizes speech from text using local on-device TTS.
    Returns audio file without internet connection.
    """
    text = request.text.strip()
    if not text:
        raise HTTPException(status_code=400, detail="Text cannot be empty.")

    out_file = TEMP_AUDIO_DIR / f"tts_{uuid.uuid4().hex}.wav"
    tts = get_tts_engine()
    res_path = tts.synthesize_to_file(text, out_file)

    if res_path and res_path.exists():
        return FileResponse(
            path=str(res_path),
            media_type="audio/wav",
            filename="answer.wav"
        )
    else:
        raise HTTPException(status_code=500, detail="Local TTS synthesis could not generate audio.")


@router.post("/ask")
async def voice_ask_constitution(
    audio: UploadFile = File(...),
    language: Optional[str] = Form(None),
    lang: Optional[str] = Query(None)
):
    """
    Complete offline voice loop:
    Microphone Audio -> Local STT -> Constitution RAG -> Local LLM -> Offline TTS
    """
    chosen_language = language or lang

    # 1. Transcribe voice question
    transcription_result = await transcribe_audio(audio, language=chosen_language)
    question = transcription_result.get("text", "").strip()

    if not question:
        raise HTTPException(status_code=400, detail="Could not detect audible speech in the uploaded audio.")

    # 2. Constitution RAG Retrieval
    retriever = get_constitution_retriever()
    retrieved_chunks = retriever.retrieve(question, top_k=3)

    # 3. Local LLM Generation
    generator = get_generator()
    answer_text, model_name, is_offline = generator.generate(
        question=question,
        retrieved_chunks=retrieved_chunks
    )

    # 4. Synthesize Answer Audio
    out_file = TEMP_AUDIO_DIR / f"voice_ans_{uuid.uuid4().hex}.wav"
    tts = get_tts_engine()
    tts_path = tts.synthesize_to_file(answer_text, out_file)

    sources = [
        {
            "article": c.article or c.schedule or "Constitution",
            "title": c.title,
            "content": c.content[:300] + ("..." if len(c.content) > 300 else "")
        }
        for c in retrieved_chunks
    ]

    return {
        "question": question,
        "answer": answer_text,
        "sources": sources,
        "offline": is_offline,
        "model_used": model_name,
        "audio_available": tts_path is not None and tts_path.exists(),
        "transcription_metadata": {
            "language": transcription_result.get("language"),
            "stage": transcription_result.get("stage"),
            "latency_seconds": transcription_result.get("latency_seconds")
        }
    }
