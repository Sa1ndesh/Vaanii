"""
Shared AI client helper with multi-model Gemini fallback + Local Ollama fallback.
Retries across multiple Gemini models (2.5-flash, 1.5-flash, 2.0-flash, 1.5-pro) to avoid 429 quota exhaustion.
"""

import os
import json
import httpx
import asyncio
from dotenv import load_dotenv

load_dotenv()

from app.core.config import OLLAMA_BASE_URL, OLLAMA_MODEL

# Persistent async client for fallback usage
_async_client = httpx.AsyncClient(timeout=10.0)

# Local trained model support
_local_model_available = False
try:
    from app.services.local_llm import chat_local
    _local_model_available = True
    print("[AI Client] Local Vani-Kanoon model available")
except Exception as e:
    print(f"[AI Client] Local model not available: {e}")

# Google GenAI Client initialization
_gemini_client = None
try:
    from google import genai
    api_key = os.getenv('GOOGLE_API_KEY') or os.getenv('GENAI_API_KEY')
    if api_key:
        _gemini_client = genai.Client(api_key=api_key)
except Exception as _e:
    print(f"[AI Client] google.genai initialization info: {_e}")

FALLBACK_MODELS = [
    "gemini-3.5-flash-lite",
    "gemini-3.5-flash",
    "gemini-2.5-flash"
]


async def _call_gemini(
    prompt: str,
    system: str = "",
    response_mime_type: str | None = None
) -> str | None:
    """Multi-model Gemini Flash generation with automatic rate-limit fallback."""
    if not _gemini_client:
        return None

    from google.genai import types

    config_kwargs = {
        "temperature": 0.3,
        "max_output_tokens": 8192
    }
    if response_mime_type:
        config_kwargs["response_mime_type"] = response_mime_type
    # Pass system prompt as a proper system_instruction so Gemini actually honours it
    if system:
        config_kwargs["system_instruction"] = system

    config = types.GenerateContentConfig(**config_kwargs)

    for model_name in FALLBACK_MODELS:
        try:
            loop = asyncio.get_event_loop()
            res = await asyncio.wait_for(
                loop.run_in_executor(
                    None,
                    lambda m=model_name: _gemini_client.models.generate_content(
                        model=m,
                        contents=prompt,
                        config=config
                    )
                ),
                timeout=8.0
            )
            if res and res.text:
                return res.text.strip()
        except Exception as err:
            print(f"[Offline/Timeout Failover] '{model_name}' skipped ({err})...")
            continue

    return None


async def chat(
    prompt: str,
    *,
    system: str = "",
    history: list[dict] | None = None,
    temperature: float = 0.3,
    max_tokens: int = 1024,
    model: str | None = None,
    format: str | None = None,
) -> str:
    """
    Send a chat request across multi-model Gemini Flash with Local/Ollama backup.
    Priority: Gemini → Local Vani-Kanoon → Ollama
    """
    # 1. Try Multi-Model Gemini Flash
    gemini_text = await _call_gemini(prompt, system=system)
    if gemini_text:
        return gemini_text

    # 2. Try Local Trained Model (Vani-Kanoon)
    if _local_model_available:
        try:
            local_text = await chat_local(prompt, system=system, temperature=temperature, max_tokens=max_tokens)
            if local_text:
                return local_text
        except Exception as e:
            print(f"[Local Model] Error: {e}, falling back to Ollama...")

    # 3. Fallback to Local Ollama
    used_model = model or OLLAMA_MODEL
    messages: list[dict] = []
    if system:
        messages.append({"role": "system", "content": system})
    if history:
        messages.extend(history)
    messages.append({"role": "user", "content": prompt})

    payload = {
        "model": used_model,
        "messages": messages,
        "stream": False,
        "options": {
            "temperature": temperature,
            "num_predict": max_tokens,
        },
    }
    if format:
        payload["format"] = format

    try:
        response = await _async_client.post(
            f"{OLLAMA_BASE_URL}/api/chat",
            json=payload,
            headers={"Content-Type": "application/json"},
        )
        response.raise_for_status()
        data = response.json()
        text = data.get("message", {}).get("content", "").strip()
        if not text:
            raise ValueError("Ollama returned an empty response.")
        return text

    except Exception as e:
        raise ValueError(f"AI response failed: {e}") from e


async def chat_json(
    prompt: str,
    *,
    system: str = "",
    temperature: float = 0.2,
    max_tokens: int = 8192,
    model: str | None = None,
) -> dict:
    """
    Returns JSON response. Uses Multi-Model Gemini with native JSON mode & robust parsing.
    """
    json_system = (system + "\n\nCRITICAL: Respond ONLY with a valid JSON object. No markdown, no code fences, no extra text.").strip()

    # 1. Try Gemini Native JSON mode
    raw = await _call_gemini(prompt, system=json_system, response_mime_type="application/json")

    # 2. Fallback to standard chat call
    if not raw:
        raw = await chat(
            prompt,
            system=json_system,
            temperature=temperature,
            max_tokens=max_tokens,
            model=model,
            format="json"
        )

    # 3. Robust JSON Extractor & Sanitizer
    cleaned = raw.strip()

    if "```" in cleaned:
        parts = cleaned.split("```")
        for part in parts:
            part_str = part.strip()
            if part_str.startswith("json"):
                part_str = part_str[4:].strip()
            if part_str.startswith("{") and part_str.endswith("}"):
                cleaned = part_str
                break

    start_idx = cleaned.find("{")
    end_idx = cleaned.rfind("}")
    if start_idx != -1 and end_idx != -1 and end_idx > start_idx:
        cleaned = cleaned[start_idx:end_idx + 1]

    try:
        return json.loads(cleaned)
    except json.JSONDecodeError as e:
        raise ValueError(f"AI JSON parse error: {e}\nRaw response: {raw[:2000]}") from e


def chat_sync(
    prompt: str,
    *,
    system: str = "",
    temperature: float = 0.3,
    max_tokens: int = 1024,
    model: str | None = None,
) -> str:
    """Synchronous version of `chat()`."""
    return asyncio.run(
        chat(prompt, system=system, temperature=temperature, max_tokens=max_tokens, model=model)
    )


def chat_json_sync(
    prompt: str,
    *,
    system: str = "",
    temperature: float = 0.2,
    max_tokens: int = 8192,
    model: str | None = None,
) -> dict:
    """Synchronous version of `chat_json()`."""
    return asyncio.run(
        chat_json(prompt, system=system, temperature=temperature, max_tokens=max_tokens, model=model)
    )
