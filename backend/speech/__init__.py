"""
Offline Speech Services package (STT and TTS).
"""

from speech.stt import OfflineSpeechToText, get_stt_engine
from speech.tts import OfflineTextToSpeech, get_tts_engine

__all__ = [
    "OfflineSpeechToText",
    "get_stt_engine",
    "OfflineTextToSpeech",
    "get_tts_engine"
]
