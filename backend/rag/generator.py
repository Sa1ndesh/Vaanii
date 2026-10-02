"""
LLM Generation Engine for Vani-Kanoon Constitution Assistant.
Supports local Ollama models (default) and optional Gemini online mode.
Guarantees 100% offline operation when AI_MODE=offline.
"""

import os
import json
import urllib.request
import urllib.error
from typing import Tuple, List, Optional
from utils.metadata import ConstitutionChunk
from rag.prompt import CONSTITUTION_SYSTEM_PROMPT, build_rag_prompt


class LocalGenerator:
    """
    Interfaces with locally running Ollama LLM (e.g. gemma3:4b, llama3, mistral)
    or provides a deterministic, zero-hallucination constitutional synthesis fallback.
    """

    def __init__(self):
        self.ollama_base_url = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434").rstrip("/")
        self.ollama_model = os.getenv("OLLAMA_MODEL", "gemma3:4b")
        self.ai_mode = os.getenv("AI_MODE", "offline").lower()

    def check_ollama_status(self) -> Tuple[bool, str, List[str]]:
        """
        Checks if the local Ollama daemon is reachable and lists available models.
        Returns: (is_available, message, available_models_list)
        """
        url = f"{self.ollama_base_url}/api/tags"
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "Vani-Kanoon/1.0"})
            with urllib.request.urlopen(req, timeout=3) as response:
                if response.status == 200:
                    data = json.loads(response.read().decode("utf-8"))
                    models = [m.get("name", "") for m in data.get("models", [])]
                    return True, f"Ollama is online at {self.ollama_base_url}", models
                return False, f"Ollama returned HTTP {response.status}", []
        except urllib.error.URLError:
            return False, f"Ollama is not running. Start with 'ollama serve' in PowerShell.", []
        except Exception as e:
            return False, f"Ollama check failed: {str(e)}", []

    def generate(
        self,
        question: str,
        retrieved_chunks: List[ConstitutionChunk],
        language: str = "en"
    ) -> Tuple[str, str, bool]:
        """
        Generates grounded legal answer using RAG context.
        Returns: (answer_text, model_identifier, is_offline)
        """
        full_prompt = build_rag_prompt(question, retrieved_chunks, language=language)

        # 1. OPTIONAL ONLINE GEMINI MODE (Only if explicitly enabled by admin)
        if self.ai_mode == "online":
            gemini_key = os.getenv("GEMINI_API_KEY", "")
            if gemini_key:
                try:
                    import google.generativeai as genai
                    genai.configure(api_key=gemini_key)
                    model = genai.GenerativeModel("gemini-1.5-flash")
                    response = model.generate_content(
                        f"{CONSTITUTION_SYSTEM_PROMPT}\n\n{full_prompt}"
                    )
                    if response and response.text:
                        return response.text.strip(), "gemini-1.5-flash (Online)", False
                except Exception as e:
                    print(f"[LocalGenerator] Online Gemini note: {e}")

        # 2. LOCAL OFFLINE OLLAMA GENERATION
        # Payload for Ollama local inference
        payload = {
            "model": self.ollama_model,
            "prompt": full_prompt,
            "system": CONSTITUTION_SYSTEM_PROMPT,
            "stream": False,
            "options": {
                "temperature": 0.2,   # Low temperature for factual fidelity
                "top_p": 0.9,
                "num_predict": 512
            }
        }

        url = f"{self.ollama_base_url}/api/generate"
        json_data = json.dumps(payload).encode("utf-8")
        req = urllib.request.Request(
            url,
            data=json_data,
            headers={"Content-Type": "application/json", "User-Agent": "Vani-Kanoon/1.0"},
            method="POST"
        )

        try:
            with urllib.request.urlopen(req, timeout=60) as response:
                if response.status == 200:
                    result = json.loads(response.read().decode("utf-8"))
                    raw_response = result.get("response", "").strip()
                    if raw_response:
                        return raw_response, f"Ollama ({self.ollama_model})", True
        except urllib.error.HTTPError as e:
            if e.code == 404:
                print(f"[LocalGenerator] Ollama model '{self.ollama_model}' not found. Run 'ollama pull {self.ollama_model}'.")
        except urllib.error.URLError:
            pass  # Ollama daemon not running
        except Exception as e:
            print(f"[LocalGenerator] Ollama connection notice: {e}")

        # 3. DETERMINISTIC ON-DEVICE RAG SYNTHESIS FALLBACK
        fallback_answer = self._synthesize_local_response(question, retrieved_chunks, language=language)
        return fallback_answer, "On-Device Constitution Engine (Direct)", True

    def _synthesize_local_response(
        self,
        question: str,
        retrieved_chunks: List[ConstitutionChunk],
        language: str = "en"
    ) -> str:
        """
        Extracts and formats verified statutory text directly from the Constitution database.
        Completely deterministic, zero-hallucination, and guaranteed 100% offline.
        """
        if not retrieved_chunks:
            return (
                "The requested specific legal query could not be matched with sufficient certainty "
                "in the offline Constitution knowledge base. Please try querying by specific Article number "
                "(e.g., 'Article 21', 'Article 14') or constitutional concept ('Fundamental Rights', 'Preamble', 'Writs')."
            )

        is_hindi = language and language.lower().startswith("hi")
        primary = retrieved_chunks[0]
        
        parts = []
        if is_hindi:
            parts.append(f"भारतीय संविधान के {primary.article or primary.schedule or 'प्रावधान'} ({primary.title}) के अनुसार:")
            parts.append(primary.content)
            if len(retrieved_chunks) > 1:
                parts.append("\nसंबंधित अन्य संवैधानिक प्रावधान:")
                for other in retrieved_chunks[1:]:
                    parts.append(f"• {other.article or other.schedule or 'अनुच्छेद'}: {other.title}")
            parts.append("\n(नोट: यह जानकारी केवल संवैधानिक व शैक्षणिक जागरूकता हेतु है, औपचारिक कानूनी सलाह नहीं।)")
        else:
            parts.append(f"Under the Constitution of India, {primary.article or primary.schedule or 'this provision'} establishes the following principles:")
            parts.append(primary.content)
            if len(retrieved_chunks) > 1:
                parts.append("\nRelated Constitutional Provisions:")
                for other in retrieved_chunks[1:]:
                    parts.append(f"- {other.article or other.schedule or 'Provision'}: {other.title}")
            parts.append("\nNote: This explanation is provided for educational and constitutional reference, not formal legal counsel.")

        return "\n\n".join(parts)


_generator_instance = None


def get_generator() -> LocalGenerator:
    global _generator_instance
    if _generator_instance is None:
        _generator_instance = LocalGenerator()
    return _generator_instance
