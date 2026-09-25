"""
Vani-Kanoon API Routes
Voice-based multilingual legal assistant with dialect intelligence and RAG.
Migrated from google-genai SDK to Ollama local LLM.
"""

from typing import Optional
from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from app.services.ollama_client import chat
from app.core.security import security_bearer, decode_access_token
from app.services.dialect_service import (
    get_states,
    get_districts,
    get_dialect_info,
    build_dialect_prompt,
    LANGUAGE_CONFIG,
)
from app.services.legal_kb import search_legal_docs

router = APIRouter(prefix="/api/vani", tags=["VaniKanoon"])


def get_optional_user(auth=Depends(security_bearer)) -> Optional[dict]:
    """Optional authentication - returns user payload if valid token, else None."""
    if not auth or not auth.credentials:
        return None
    payload = decode_access_token(auth.credentials)
    if not payload or "sub" not in payload:
        return None
    return payload


def _build_fallback_answer(query: str, relevant_docs: list, language: str = "english") -> str:
    lang = language.lower()
    
    # Professional translations for local legal knowledge base
    fallbacks = {
        "kannada": {
            "intro": "ಭಾರತೀಯ ಕಾನೂನು ಜ್ಞಾನಕೋಶದ ಪ್ರಕಾರ: ",
            "help": "ಪ್ರಮುಖ ಕಾನೂನು ನಿಯಮಗಳು: ",
            "advice": "\n\nಕಾನೂನು ಸಲಹೆ: ಎಲ್ಲಾ ದಾಖಲೆಗಳ ಪ್ರತಿಗಳನ್ನು ಕಾಯ್ದಿರಿಸಿ ಮತ್ತು ಅರ್ಹ ವಕೀಲರನ್ನು ಸಂಪರ್ಕಿಸಿ."
        },
        "hindi": {
            "intro": "भारतीय कानूनी ज्ञानकोश के अनुसार: ",
            "help": "मुख्य कानूनी प्रावधान: ",
            "advice": "\n\nकानूनी मार्गदर्शन: सभी प्रासंगिक दस्तावेजों की प्रतियां सुरक्षित रखें और योग्य वकील से परामर्श लें।"
        },
        "marathi": {
            "intro": "भारतीय कायदेशीर ज्ञानकोशानुसार: ",
            "help": "महत्त्वाचे कायदेशीर नियम: ",
            "advice": "\n\nकायदेशीर मार्गदर्शन: सर्व आवश्यक कागदपत्रांच्या प्रती ठेवा आणि पात्र वकिलाचा सल्ला घ्या."
        },
        "english": {
            "intro": "Based on the Indian Legal Knowledge Base: ",
            "help": "Key Legal Provisions: ",
            "advice": "\n\nLegal Guidance: Keep copies of all relevant documents, adhere to legal deadlines, and consult a qualified advocate for official proceedings."
        }
    }
    
    fb = fallbacks.get(lang, fallbacks["english"])
    
    if relevant_docs:
        legal_points = " \n".join(
            f"• {doc['title']}: {doc['content']}" for doc in relevant_docs
        )
        return f"{fb['intro']}\n{legal_points}{fb['advice']}"
    
    return f"{fb['intro']} {fb['advice']}"



# ──────────────────────────────────────────────
# Pydantic Models
# ──────────────────────────────────────────────

class VaniAskRequest(BaseModel):
    language: str
    district: str
    query: str

class TTSRequest(BaseModel):
    text: str
    language: str


# ──────────────────────────────────────────────
# 1. GET SUPPORTED LANGUAGES
# ──────────────────────────────────────────────

@router.get("/languages")
def get_languages(current_user: Optional[dict] = Depends(get_optional_user)):
    return {
        "languages": [
            {"code": "kannada", "name": "ಕನ್ನಡ",  "name_en": "Kannada", "tts_lang": "kn-IN"},
            {"code": "marathi", "name": "मराठी",   "name_en": "Marathi", "tts_lang": "mr-IN"},
            {"code": "hindi",   "name": "हिंदी",   "name_en": "Hindi",   "tts_lang": "hi-IN"},
            {"code": "english", "name": "English",  "name_en": "English", "tts_lang": "en-IN"},
        ]
    }


# ──────────────────────────────────────────────
# 2. GET STATES FOR A LANGUAGE
# ──────────────────────────────────────────────

@router.get("/states/{language}")
def get_language_states(language: str, current_user: Optional[dict] = Depends(get_optional_user)):
    states = get_states(language)
    if not states:
        raise HTTPException(status_code=404, detail=f"Language '{language}' not supported.")
    return {"language": language, "states": states}


# ──────────────────────────────────────────────
# 3. GET DISTRICTS FOR A LANGUAGE & STATE
# ──────────────────────────────────────────────

@router.get("/districts/{language}")
def get_language_districts(language: str, state: str = None, current_user: Optional[dict] = Depends(get_optional_user)):
    districts = get_districts(language, state)
    if not districts:
        raise HTTPException(status_code=404, detail=f"Language '{language}' not supported.")
    return {"language": language, "state": state, "districts": districts}


# ──────────────────────────────────────────────
# 3. MAIN ASK ENDPOINT
# ──────────────────────────────────────────────

@router.post("/ask")
async def vani_ask(request: VaniAskRequest, current_user: Optional[dict] = Depends(get_optional_user)):
    relevant_docs = []

    try:
        # Step 1: RAG keyword search
        relevant_docs = search_legal_docs(request.query, top_k=2)
        rag_snippet = ""
        if relevant_docs:
            rag_snippet = "Relevant law: " + " | ".join(
                f"{d['title']}: {d['content'][:120]}" for d in relevant_docs
            ) + "\n\n"

        # Step 2: Build dialect-aware prompt
        user_prompt = rag_snippet + build_dialect_prompt(
            request.language, request.district, request.query
        )

        # Step 3: Call Ollama
        lang_name = request.language.capitalize()
        lang_script = {
            "kannada": "ಕನ್ನಡ",
            "hindi": "हिंदी",
            "marathi": "मराठी",
            "english": "English"
        }.get(request.language.lower(), request.language)

        system_prompt = f"""You are Vani-Kanoon, an expert Indian legal assistant.

CRITICAL LANGUAGE REQUIREMENT:
- You MUST write your ENTIRE response in {lang_name} ({lang_script}) script.
- DO NOT use English at all. Not even for legal terms.
- Translate ALL legal terms into {lang_name}.
- If you write even ONE English word, you have FAILED.

Example for {lang_name}:
- "Section 302" → write it in {lang_script} script
- "Rent Control Act" → translate to {lang_script}
- "landlord", "tenant" → translate to {lang_script}

Your response must be 100% in {lang_script} script. Zero English."""

        answer = await chat(
            user_prompt,
            system=system_prompt,
            temperature=0.4,
            max_tokens=1024,
        )
        answer = answer.replace("*", "").replace("#", "").strip()

        # Step 4: Dialect info for frontend
        dialect_info = get_dialect_info(request.language, request.district)
        lang_config = LANGUAGE_CONFIG.get(request.language.lower(), {})

        return {
            "language": request.language,
            "district": request.district,
            "dialect": dialect_info.get("dialect", "Standard"),
            "query": request.query,
            "answer": answer,
            "tts_lang": lang_config.get("tts_lang", "en-IN"),
            "relevant_docs": [{"title": d["title"], "id": d["id"]} for d in relevant_docs]
        }

    except HTTPException:
        raise
    except Exception as e:
        err_str = str(e)
        err_type = type(e).__name__

        print(f"\n[Vani-Kanoon] ERROR TYPE : {err_type}")
        print(f"[Vani-Kanoon] ERROR MSG  : {err_str[:500]}\n")

        # Graceful fallback
        fallback = _build_fallback_answer(request.query, relevant_docs, language=request.language)
        dialect_info = get_dialect_info(request.language, request.district)
        lang_config = LANGUAGE_CONFIG.get(request.language.lower(), {})
        return {
            "language": request.language,
            "district": request.district,
            "dialect": dialect_info.get("dialect", "Standard"),
            "query": request.query,
            "answer": fallback,
            "tts_lang": lang_config.get("tts_lang", "en-IN"),
            "relevant_docs": [{"title": d["title"], "id": d["id"]} for d in relevant_docs],
        }


# ──────────────────────────────────────────────
# 4. DIALECT INFO
# ──────────────────────────────────────────────

@router.get("/dialect-info/{language}/{district}")
def dialect_info_endpoint(language: str, district: str, current_user: Optional[dict] = Depends(get_optional_user)):
    info = get_dialect_info(language, district)
    return {"language": language, "district": district, **info}


# ──────────────────────────────────────────────
# 5. TEXT-TO-SPEECH (Microsoft Neural HD Human Voice via edge-tts)
# ──────────────────────────────────────────────

NEURAL_VOICES = {
    "hindi": "hi-IN-SwaraNeural",
    "kannada": "kn-IN-SapnaNeural",
    "marathi": "mr-IN-AarohiNeural",
    "english": "en-IN-NeerjaNeural"
}

GTTS_LANG_MAP = {"kannada": "kn", "marathi": "mr", "hindi": "hi", "english": "en"}

@router.post("/tts")
async def text_to_speech(request: TTSRequest, current_user: Optional[dict] = Depends(get_optional_user)):
    from fastapi.responses import Response
    import io

    lang = request.language.lower()
    voice = NEURAL_VOICES.get(lang, "hi-IN-SwaraNeural")

    # Priority 1: Edge-TTS Microsoft Neural HD Human Voice
    try:
        import edge_tts
        import re

        clean_text = re.sub(r'[*#_`~>\[\]()]', ' ', request.text)
        clean_text = re.sub(r'\s+', ' ', clean_text).strip()
        if not clean_text:
            clean_text = request.text

        communicate = edge_tts.Communicate(clean_text, voice)
        buf = io.BytesIO()
        async for chunk in communicate.stream():
            if chunk["type"] == "audio":
                buf.write(chunk["data"])
        
        audio = buf.getvalue()
        if audio:
            return Response(
                content=audio, media_type="audio/mpeg",
                headers={"Content-Length": str(len(audio)),
                         "Content-Disposition": "inline; filename=vani_speech.mp3"}
            )
    except Exception as err:
        print(f"[TTS Warning] Edge-TTS error ({err}), falling back to gTTS...")

    # Priority 2: Fallback to gTTS
    lang_code = GTTS_LANG_MAP.get(lang, "en")
    try:
        from gtts import gTTS
        buf = io.BytesIO()
        gTTS(text=request.text, lang=lang_code, slow=False).write_to_fp(buf)
        audio = buf.getvalue()
        return Response(
            content=audio, media_type="audio/mpeg",
            headers={"Content-Length": str(len(audio)),
                     "Content-Disposition": "inline; filename=vani_speech.mp3"}
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"TTS error: {str(e)}")
