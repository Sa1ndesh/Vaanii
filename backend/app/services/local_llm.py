"""
Local Legal LLM Service - Uses your trained Vani-Kanoon model
"""

import os
import torch
from pathlib import Path

# Model paths - update if needed
ADAPTER_PATH = os.getenv("VANI_MODEL_PATH", "C:/Users/Sande/Downloads/vani-kanoon-legal")
BASE_MODEL = os.getenv("VANI_BASE_MODEL", "E:/models/tinyllama")

_model = None
_tokenizer = None
_device = None


def _load_model():
    """Load the fine-tuned model (lazy loading)"""
    global _model, _tokenizer, _device

    if _model is not None:
        return _model, _tokenizer

    print(f"[LocalLLM] Loading model from {ADAPTER_PATH}...")

    from transformers import AutoModelForCausalLM, AutoTokenizer
    from peft import PeftModel

    # Determine device
    _device = "cuda" if torch.cuda.is_available() else "cpu"
    print(f"[LocalLLM] Using device: {_device}")

    # Load tokenizer
    _tokenizer = AutoTokenizer.from_pretrained(ADAPTER_PATH)
    _tokenizer.pad_token = _tokenizer.eos_token

    # Load base model
    base_model = AutoModelForCausalLM.from_pretrained(
        BASE_MODEL,
        torch_dtype=torch.float16 if _device == "cuda" else torch.float32,
        device_map="auto" if _device == "cuda" else None,
        low_cpu_mem_usage=True,
    )

    # Load LoRA adapter
    _model = PeftModel.from_pretrained(base_model, ADAPTER_PATH)
    _model.eval()

    if _device == "cpu":
        _model = _model.to(_device)

    print(f"[LocalLLM] Model loaded successfully!")
    return _model, _tokenizer


def generate_response(
    prompt: str,
    system: str = "",
    max_tokens: int = 512,
    temperature: float = 0.7,
) -> str:
    """Generate response using local model"""
    model, tokenizer = _load_model()

    # Format prompt
    if system:
        full_prompt = f"<|system|>\n{system}\n<|user|>\n{prompt}\n<|assistant|>\n"
    else:
        full_prompt = f"<|user|>\n{prompt}\n<|assistant|>\n"

    # Tokenize
    inputs = tokenizer(full_prompt, return_tensors="pt", truncation=True, max_length=1024)
    inputs = {k: v.to(_device) for k, v in inputs.items()}

    # Generate
    with torch.no_grad():
        outputs = model.generate(
            **inputs,
            max_new_tokens=max_tokens,
            temperature=temperature,
            do_sample=True,
            top_p=0.9,
            repetition_penalty=1.1,
            pad_token_id=tokenizer.eos_token_id,
        )

    # Decode
    response = tokenizer.decode(outputs[0], skip_special_tokens=True)

    # Extract assistant response
    if "<|assistant|>" in response:
        response = response.split("<|assistant|>")[-1].strip()

    return response


async def chat_local(
    prompt: str,
    system: str = "",
    temperature: float = 0.7,
    max_tokens: int = 512,
) -> str:
    """Async wrapper for local model generation"""
    import asyncio

    loop = asyncio.get_event_loop()
    return await loop.run_in_executor(
        None,
        lambda: generate_response(prompt, system, max_tokens, temperature)
    )


# Quick test
if __name__ == "__main__":
    print("\nTesting local model...")
    response = generate_response(
        "What is the punishment for murder under BNS 2023?",
        system="You are Vani-Kanoon, an expert Indian legal assistant."
    )
    print(f"\nResponse:\n{response}")
