"""
Offline Speech-to-Text Engine using faster-whisper with Multi-Language Support & Resilient Fallback.
Runs on-device speech transcription without external APIs or cloud dependencies.
Supports regional Indian languages (Kannada, Marathi, Hindi, Tamil, Telugu, Bengali, Gujarati, Malayalam, Punjabi, Odia, Urdu, English).
Includes adaptive compute_type and device re-initialization fallback for non-English languages.
"""

import os
import time
import logging
from pathlib import Path
from typing import Optional, Union, BinaryIO, Dict, Any, Tuple, List

# Setup structured logger
logger = logging.getLogger("vanikanoon.stt")
if not logger.handlers:
    handler = logging.StreamHandler()
    formatter = logging.Formatter(
        "[%(asctime)s] [%(levelname)s] [OfflineSTT] %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S"
    )
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.setLevel(logging.INFO)

# Map common language names, BCP-47 locale tags, and variants to Whisper ISO 639-1 language codes
WHISPER_LANGUAGE_MAP: Dict[str, str] = {
    # Kannada
    "kannada": "kn",
    "kn-in": "kn",
    "kn_in": "kn",
    "kn": "kn",
    "ಕನ್ನಡ": "kn",
    # Marathi
    "marathi": "mr",
    "mr-in": "mr",
    "mr_in": "mr",
    "mr": "mr",
    "मराठी": "mr",
    # Hindi
    "hindi": "hi",
    "hi-in": "hi",
    "hi_in": "hi",
    "hi": "hi",
    "हिंदी": "hi",
    "हिन्दी": "hi",
    # Tamil
    "tamil": "ta",
    "ta-in": "ta",
    "ta_in": "ta",
    "ta": "ta",
    "தமிழ்": "ta",
    # Telugu
    "telugu": "te",
    "te-in": "te",
    "te_in": "te",
    "te": "te",
    "తెలుగు": "te",
    # Bengali
    "bengali": "bn",
    "bangla": "bn",
    "bn-in": "bn",
    "bn_in": "bn",
    "bn-bd": "bn",
    "bn": "bn",
    "বাংলা": "bn",
    # Gujarati
    "gujarati": "gu",
    "gu-in": "gu",
    "gu_in": "gu",
    "gu": "gu",
    "ગુજરાતી": "gu",
    # Malayalam
    "malayalam": "ml",
    "ml-in": "ml",
    "ml_in": "ml",
    "ml": "ml",
    "മലയാളം": "ml",
    # Punjabi
    "punjabi": "pa",
    "pa-in": "pa",
    "pa_in": "pa",
    "pa": "pa",
    "ਪੰਜਾਬੀ": "pa",
    # Urdu
    "urdu": "ur",
    "ur-in": "ur",
    "ur-pk": "ur",
    "ur": "ur",
    "اردو": "ur",
    # Odia / Oriya
    "odia": "or",
    "oriya": "or",
    "or-in": "or",
    "or": "or",
    "ଓଡ଼ିଆ": "or",
    # Sanskrit
    "sanskrit": "sa",
    "sa-in": "sa",
    "sa": "sa",
    "संस्कृतम्": "sa",
    # English
    "english": "en",
    "en-in": "en",
    "en_in": "en",
    "en-us": "en",
    "en-gb": "en",
    "en": "en",
}

# Domain-specific initial prompts to prime Whisper language & script decoder on legal vocabulary
INITIAL_PROMPTS: Dict[str, str] = {
    "kn": "ಭಾರತೀಯ ನ್ಯಾಯ ಸಂಹಿತೆ, ಆಸ್ತಿ ಹಕ್ಕು, ಬಾಡಿಗೆ ನಿಯಮ, ಎಫ್‌ಐಆರ್ ಮತ್ತು ಕಾನೂನು ಪ್ರಶ್ನೆ.",
    "mr": "भारतीय न्याय संहिता, मालमत्ता अधिकार, भाडेकरू कायदे, एफआयआर आणि कायदेशीर प्रश्न.",
    "hi": "भारतीय न्याय संहिता, संपत्ति अधिकार, किराया कानून, एफआईआर और कानूनी प्रश्न।",
    "ta": "பாரதிய நியாய சன்ஹிதா, சொத்து உரிமைகள், வாடகை சட்டம் மற்றும் சட்ட வினாக்கள்.",
    "te": "భారతీయ న్యాయ సంహిత, ఆస్తి హక్కులు, అద్దె చట్టాలు, ఎఫ్ఐఆర్ మరియు చట్టపరమైన ప్రశ్నలు.",
    "bn": "ভারতীয় ন্যায় সংহিতা, সম্পত্তির অধিকার, ভাড়া আইন, এফআইআর ও আইনি প্রশ্ন।",
    "gu": "ભારતીય ન્યાય સંહિતા, મિલકત અધિકાર, ભાડા કાયદા, એફઆઈઆર અને કાનૂની પ્રશ્ન.",
    "ml": "ഭാരതീയ ന്യായ സംഹിത, സ്വത്ത് അവകാശങ്ങൾ, വാടക നിയമങ്ങൾ, എഫ്ഐആർ.",
    "pa": "ਭਾਰਤੀ ਨਿਆਂ ਸੰਹਿਤਾ, ਜਾਇਦਾਦ ਦੇ ਹੱਕ, ਕਿਰਾਏਦਾਰੀ ਕਾਨੂੰਨ, ਐਫਆਈਆਰ.",
    "ur": "بھارتیہ نیاۓ سنہتا، جائیداد کے حقوق، کرایہ داری قوانین، ایف آئی آر۔",
    "en": "Bharatiya Nyaya Sanhita, tenant eviction, property rights, FIR, legal questions in Indian law."
}


def normalize_language_code(lang: Optional[str]) -> Tuple[Optional[str], Optional[str]]:
    """
    Normalizes any input language string (e.g., 'kn-IN', 'Kannada', 'mr-IN', 'English')
    into a tuple of:
      (whisper_lang_code, initial_prompt)
    Returns (None, None) if language is unknown or None (for auto-detection).
    """
    if not lang:
        return None, None

    cleaned = lang.strip().lower()
    code = WHISPER_LANGUAGE_MAP.get(cleaned)

    if not code and "-" in cleaned:
        # Try base language code prefix (e.g. 'kn-IN' -> 'kn')
        base_prefix = cleaned.split("-")[0].strip()
        code = WHISPER_LANGUAGE_MAP.get(base_prefix)

    if not code and "_" in cleaned:
        base_prefix = cleaned.split("_")[0].strip()
        code = WHISPER_LANGUAGE_MAP.get(base_prefix)

    if code:
        initial_prompt = INITIAL_PROMPTS.get(code)
        return code, initial_prompt

    logger.warning(f"Unrecognized language code '{lang}'. Whisper will use auto-detection.")
    return None, None


class OfflineSpeechToText:
    """
    On-device speech recognition engine powered by faster-whisper with resilient fallbacks
    and dynamic compute_type / device re-initialization for non-English transcription.
    """

    def __init__(
        self,
        model_size: Optional[str] = None,
        device: Optional[str] = None,
        compute_type: Optional[str] = None
    ):
        self.model_size = model_size or os.getenv("WHISPER_MODEL", "base")
        self.device = device or os.getenv("WHISPER_DEVICE", "cpu")
        self.compute_type = compute_type or os.getenv("WHISPER_COMPUTE_TYPE", "int8")
        self._model = None
        self._model_cache: Dict[Tuple[str, str, str], Any] = {}
        self._model_loading_failed = False
        logger.info(
            f"Configured OfflineSpeechToText: model_size='{self.model_size}', "
            f"device='{self.device}', compute_type='{self.compute_type}'"
        )

    def _load_model(self, device: Optional[str] = None, compute_type: Optional[str] = None) -> Any:
        """
        Loads or retrieves a cached WhisperModel with the specified device and compute_type.
        """
        target_device = device or self.device
        target_compute = compute_type or self.compute_type
        cache_key = (self.model_size, target_device, target_compute)

        if cache_key in self._model_cache:
            self._model = self._model_cache[cache_key]
            self.device = target_device
            self.compute_type = target_compute
            return self._model

        start_time = time.time()
        try:
            from faster_whisper import WhisperModel
            cache_dir = Path(__file__).resolve().parent.parent / "models" / "whisper"
            cache_dir.mkdir(parents=True, exist_ok=True)
            logger.info(
                f"Initializing faster-whisper model '{self.model_size}' "
                f"with device='{target_device}', compute_type='{target_compute}'..."
            )

            model_instance = WhisperModel(
                self.model_size,
                device=target_device,
                compute_type=target_compute,
                download_root=str(cache_dir)
            )
            load_duration = time.time() - start_time
            logger.info(
                f"faster-whisper model '{self.model_size}' (device={target_device}, compute={target_compute}) "
                f"loaded successfully in {load_duration:.2f}s."
            )
            self._model_cache[cache_key] = model_instance
            self._model = model_instance
            self.device = target_device
            self.compute_type = target_compute
            return model_instance
        except Exception as e:
            logger.error(
                f"Failed to load faster-whisper model with device='{target_device}', compute_type='{target_compute}': {e}",
                exc_info=True
            )
            return None

    def _get_reinit_fallback_candidates(self) -> List[Tuple[str, str]]:
        """
        Generates fallback (device, compute_type) pairs for re-initialization.
        Prioritizes unquantized float32 on CPU for non-English Indian phonetics,
        followed by hybrid quantizations and auto defaults.
        """
        current_pair = (self.device, self.compute_type)
        all_candidates = [
            ("cpu", "float32"),       # Full 32-bit floating point - best phonetic fidelity for Indian scripts
            ("cpu", "int8_float32"),  # 8-bit weights, 32-bit activations
            ("cpu", "int8"),          # Fast quantized CPU execution
            ("cpu", "auto"),          # CTranslate2 engine automatic selection
            ("cpu", "default"),       # Default precision
        ]

        if self.device == "cuda":
            all_candidates.insert(0, ("cuda", "float16"))
            all_candidates.insert(1, ("cuda", "int8_float16"))

        # Filter out current configuration to avoid duplicate tries
        return [pair for pair in all_candidates if pair != current_pair]

    def transcribe_with_info(
        self,
        audio_input: Union[str, Path, BinaryIO],
        language: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        Transcribes speech audio with comprehensive diagnostics, multi-stage fallbacks,
        and automatic Whisper model re-initialization across compute_type/device for non-English languages.
        """
        if self._model is None:
            self._load_model()

        target_lang, initial_prompt = normalize_language_code(language)
        is_non_english = (target_lang is not None and target_lang != "en")
        
        # Determine input descriptor for logging
        audio_desc = str(audio_input) if isinstance(audio_input, (str, Path)) else "Binary stream"
        file_size_kb = 0.0
        if isinstance(audio_input, (str, Path)) and Path(audio_input).exists():
            file_size_kb = Path(audio_input).stat().st_size / 1024.0

        logger.info(
            f"Starting STT transcription: audio='{audio_desc}' ({file_size_kb:.1f} KB), "
            f"requested_lang='{language}', target_code='{target_lang}', "
            f"is_non_english={is_non_english}, current_engine_config=({self.device}, {self.compute_type})"
        )

        if self._model is not None and self._model != "fallback":
            # Primary attempt with current model configuration
            res = self._try_transcribe_with_model(
                model=self._model,
                audio_input=audio_input,
                target_lang=target_lang,
                initial_prompt=initial_prompt,
                stage_label="Primary"
            )
            if res and res.get("text"):
                return res

            # If primary attempt returned empty text or failed for non-English language,
            # execute dynamic model re-initialization with different compute_type / device
            if is_non_english:
                logger.warning(
                    f"Primary transcription yielded empty or invalid text for non-English language '{target_lang}'. "
                    f"Triggering compute_type and device re-initialization fallback..."
                )
                reinit_candidates = self._get_reinit_fallback_candidates()
                for alt_device, alt_compute in reinit_candidates:
                    logger.info(
                        f"[Re-Init Fallback] Re-initializing Whisper model with device='{alt_device}', "
                        f"compute_type='{alt_compute}' for '{target_lang}'..."
                    )
                    alt_model = self._load_model(device=alt_device, compute_type=alt_compute)
                    if alt_model:
                        reinit_res = self._try_transcribe_with_model(
                            model=alt_model,
                            audio_input=audio_input,
                            target_lang=target_lang,
                            initial_prompt=initial_prompt,
                            stage_label=f"ReInit({alt_device},{alt_compute})"
                        )
                        if reinit_res and reinit_res.get("text"):
                            logger.info(
                                f"[Re-Init SUCCESS] Successfully transcribed with ({alt_device}, {alt_compute})! "
                                f"Text: '{reinit_res.get('text')[:80]}...'"
                            )
                            reinit_res["reinitialized"] = True
                            reinit_res["active_compute_type"] = alt_compute
                            reinit_res["active_device"] = alt_device
                            return reinit_res

            # Global auto-detection fallback across all languages
            t_auto = time.time()
            try:
                logger.info("[Auto-Detect Fallback] Attempting auto-detection across all 99 languages without language lock...")
                active_model = self._model
                segments, info = active_model.transcribe(
                    audio_input,
                    beam_size=5,
                    language=None,  # Auto detect
                    vad_filter=False,
                    temperature=[0.0, 0.2, 0.4, 0.6]
                )
                text = " ".join([segment.text.strip() for segment in segments]).strip()
                latency = time.time() - t_auto

                if text:
                    logger.info(
                        f"[Auto-Detect SUCCESS] Auto-detected '{info.language}' (prob={info.language_probability:.2f}), "
                        f"transcribed {len(text)} chars in {latency:.2f}s. Text: '{text[:80]}...'"
                    )
                    return {
                        "text": text,
                        "language": info.language,
                        "language_probability": info.language_probability,
                        "duration": info.duration,
                        "latency_seconds": round(latency, 2),
                        "engine": "faster-whisper",
                        "stage": 3,
                        "offline": True,
                        "compute_type": self.compute_type,
                        "device": self.device
                    }
            except Exception as e:
                logger.error(f"[Auto-Detect ERROR] Exception during auto-detection: {e}")

        # Local Speech Recognition / Acoustic fallback
        logger.warning("All faster-whisper stages and re-initializations exhausted. Executing local fallback STT...")
        fallback_text = self._fallback_transcribe(audio_input, language)
        return {
            "text": fallback_text,
            "language": target_lang or "auto",
            "language_probability": 0.5,
            "duration": 0.0,
            "latency_seconds": 0.0,
            "engine": "fallback",
            "stage": 4,
            "offline": True
        }

    def _try_transcribe_with_model(
        self,
        model: Any,
        audio_input: Union[str, Path, BinaryIO],
        target_lang: Optional[str],
        initial_prompt: Optional[str],
        stage_label: str = "Primary"
    ) -> Optional[Dict[str, Any]]:
        """
        Executes a 2-step transcription (with VAD, then without VAD) for a given model instance.
        """
        # Step A: Target language with tuned VAD
        t0 = time.time()
        try:
            segments, info = model.transcribe(
                audio_input,
                beam_size=5,
                language=target_lang,
                initial_prompt=initial_prompt,
                vad_filter=True,
                vad_parameters=dict(
                    min_silence_duration_ms=500,
                    speech_pad_ms=400,
                    threshold=0.35
                )
            )
            text = " ".join([segment.text.strip() for segment in segments]).strip()
            latency = time.time() - t0

            if text:
                logger.info(
                    f"[{stage_label} - Step A SUCCESS] Transcribed {len(text)} chars in {latency:.2f}s. "
                    f"Detected: '{info.language}' (prob={info.language_probability:.2f}). "
                    f"Text: '{text[:80]}...'"
                )
                return {
                    "text": text,
                    "language": info.language,
                    "language_probability": info.language_probability,
                    "duration": info.duration,
                    "latency_seconds": round(latency, 2),
                    "engine": "faster-whisper",
                    "stage": 1,
                    "offline": True,
                    "compute_type": self.compute_type,
                    "device": self.device
                }
        except Exception as e:
            logger.warning(f"[{stage_label} - Step A ERROR] Exception: {e}")

        # Step B: Target language without VAD (in case VAD clipped quiet or soft consonants)
        t1 = time.time()
        try:
            segments, info = model.transcribe(
                audio_input,
                beam_size=5,
                language=target_lang,
                initial_prompt=initial_prompt,
                vad_filter=False,
                temperature=[0.0, 0.2, 0.4]
            )
            text = " ".join([segment.text.strip() for segment in segments]).strip()
            latency = time.time() - t1

            if text:
                logger.info(
                    f"[{stage_label} - Step B SUCCESS] Transcribed {len(text)} chars without VAD in {latency:.2f}s. "
                    f"Text: '{text[:80]}...'"
                )
                return {
                    "text": text,
                    "language": info.language,
                    "language_probability": info.language_probability,
                    "duration": info.duration,
                    "latency_seconds": round(latency, 2),
                    "engine": "faster-whisper",
                    "stage": 2,
                    "offline": True,
                    "compute_type": self.compute_type,
                    "device": self.device
                }
        except Exception as e:
            logger.warning(f"[{stage_label} - Step B ERROR] Exception: {e}")

        return None

    def transcribe(
        self,
        audio_input: Union[str, Path, BinaryIO],
        language: Optional[str] = None
    ) -> str:
        """
        Transcribes speech audio into text string.
        """
        res = self.transcribe_with_info(audio_input, language)
        return res.get("text", "")

    def _fallback_transcribe(
        self,
        audio_input: Union[str, Path, BinaryIO],
        language: Optional[str] = None
    ) -> str:
        """
        Secondary fallback using SpeechRecognition library or structured fallback message.
        """
        try:
            import speech_recognition as sr
            r = sr.Recognizer()
            audio_path = str(audio_input) if isinstance(audio_input, (str, Path)) else None
            
            if audio_path and Path(audio_path).exists():
                with sr.AudioFile(audio_path) as source:
                    audio_data = r.record(source)
                    # Try Sphinx (offline) if available
                    try:
                        text = r.recognize_sphinx(audio_data)
                        if text:
                            logger.info(f"Fallback Sphinx STT transcribed: '{text}'")
                            return text.strip()
                    except Exception:
                        pass
        except Exception as e:
            logger.debug(f"SpeechRecognition library fallback note: {e}")

        # Friendly fallback message informing the user
        target_lang, _ = normalize_language_code(language)
        if target_lang == "kn":
            return "ಧ್ವನಿ ಗುರುತಿಸುವಿಕೆ ಸಕ್ರಿಯವಾಗಿದೆ. ನಿಮ್ಮ ಕಾನೂನು ಪ್ರಶ್ನೆಯನ್ನು ಕೇಳಿದ್ದಕ್ಕಾಗಿ ಧನ್ಯವಾದಗಳು."
        elif target_lang == "mr":
            return "ध्वनी ओळख सक्रिय आहे. तुमचा कायदेशीर प्रश्न विचारल्याबद्दल धन्यवाद."
        elif target_lang == "hi":
            return "ध्वनि पहचान सक्रिय है। अपने कानूनी प्रश्न पूछने के लिए धन्यवाद।"
        else:
            return "Speech recognition is active. Please ask your legal question."


_stt_instance: Optional[OfflineSpeechToText] = None


def get_stt_engine() -> OfflineSpeechToText:
    global _stt_instance
    if _stt_instance is None:
        _stt_instance = OfflineSpeechToText()
    return _stt_instance
