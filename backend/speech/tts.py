"""
Offline Text-to-Speech Engine.
Supports Piper (preferred) or native pyttsx3 (SAPI5/eSpeak) for 100% on-device audio synthesis.
"""

import os
import shutil
import subprocess
from pathlib import Path
from typing import Optional


class OfflineTextToSpeech:
    """
    On-device audio synthesis engine requiring zero cloud connectivity.
    """

    def __init__(self, engine_type: Optional[str] = None):
        self.engine_type = engine_type or os.getenv("TTS_ENGINE", "pyttsx3").lower()
        self._piper_binary = shutil.which("piper") or shutil.which("piper.exe")

    def synthesize_to_file(self, text: str, output_path: Path) -> Optional[Path]:
        """
        Synthesizes text into an offline WAV audio file.
        """
        if not text:
            return None

        output_path.parent.mkdir(parents=True, exist_ok=True)

        # 1. Try Piper if binary exists
        if self._piper_binary and (self.engine_type == "piper" or Path(self._piper_binary).exists()):
            try:
                # Piper CLI invocation
                model_path = os.getenv("PIPER_MODEL_PATH", "./models/piper/en_US-lessac-medium.onnx")
                if Path(model_path).exists():
                    process = subprocess.Popen(
                        [self._piper_binary, "--model", model_path, "--output_file", str(output_path)],
                        stdin=subprocess.PIPE,
                        stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE
                    )
                    process.communicate(input=text.encode("utf-8"))
                    if output_path.exists():
                        return output_path
            except Exception as e:
                print(f"[OfflineTextToSpeech] Piper notice: {e}, falling back to pyttsx3.")

        # 2. Use pyttsx3 (Native Windows SAPI5 / Linux eSpeak)
        try:
            import pyttsx3
            engine = pyttsx3.init()
            engine.setProperty("rate", 160)  # Moderate speech speed
            engine.setProperty("volume", 0.9)
            engine.save_to_file(text, str(output_path))
            engine.runAndWait()
            if output_path.exists():
                return output_path
        except Exception as e:
            print(f"[OfflineTextToSpeech] pyttsx3 synthesis error: {e}")

        return None


_tts_instance: Optional[OfflineTextToSpeech] = None


def get_tts_engine() -> OfflineTextToSpeech:
    global _tts_instance
    if _tts_instance is None:
        _tts_instance = OfflineTextToSpeech()
    return _tts_instance
