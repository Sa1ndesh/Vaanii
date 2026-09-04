import os
from dotenv import load_dotenv

# Load the .env file (backend/.env is 3 levels up from app/core/config.py)
env_path = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), '.env')
load_dotenv(dotenv_path=env_path)

# ---------------------------------------------------------------------------
# Ollama configuration (local LLM — no API keys required)
# ---------------------------------------------------------------------------
OLLAMA_BASE_URL: str = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
OLLAMA_MODEL: str    = os.getenv("OLLAMA_MODEL", "llama3.2")

# Legacy Google key vars kept as empty strings so old imports don't crash.
# All services have been migrated to Ollama; these are no longer used.
GENAI_API_KEY             = ""
GOOGLE_API_KEY            = ""
DOCUMENT_GENERATOR_API_KEY = ""
RENT_SALE_API_KEY         = ""
LEASE_DEED_API_KEY        = ""
CASE_SUMMARIZER_API_KEY   = ""
FIR_ANALYZER_API_KEY      = ""
LEGAL_NOTICE_API_KEY      = ""
FAQ_BUILDER_API_KEY       = ""
LEARNING_HUB_API_KEY      = ""
LEGAL_RESEARCH_API_KEY    = ""

# DB config (unchanged)
DB_USER     = os.getenv("DB_USER", "root")
DB_PASSWORD = os.getenv("DB_PASSWORD", "")
DB_HOST     = os.getenv("DB_HOST", "localhost")
DB_NAME     = os.getenv("DB_NAME", "ai_legal_assistant")

print(f"[config] Ollama -> {OLLAMA_BASE_URL}  model={OLLAMA_MODEL}")