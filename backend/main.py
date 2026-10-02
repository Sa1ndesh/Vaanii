"""
Vani-Kanoon Backend Application
FastAPI Server providing Offline Indian Constitution RAG Assistant,
Local Speech Processing, and Multilingual Voice Legal Intelligence.
"""

import os
import sys
from pathlib import Path
from dotenv import load_dotenv

# Ensure backend root directory is in Python path
BACKEND_ROOT = Path(__file__).resolve().parent
if str(BACKEND_ROOT) not in sys.path:
    sys.path.insert(0, str(BACKEND_ROOT))

# Load .env file
load_dotenv(dotenv_path=BACKEND_ROOT / ".env")

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

# Import Offline Constitution and Voice Routers
from api.constitution import router as constitution_router
from api.voice import router as voice_router

# Initialize FastAPI Application
app = FastAPI(
    title="Vani-Kanoon: Legal AI & Offline Constitution Assistant",
    description="On-device Offline Indian Constitution RAG Assistant with Local Speech Processing and Legal Intelligence",
    version="2.0.0"
)

# Configure CORS for React Vite Frontend and Android Mobile Network
allowed_origins_env = os.getenv("ALLOWED_ORIGINS", "")
if allowed_origins_env:
    origins = [o.strip() for o in allowed_origins_env.split(",") if o.strip()]
else:
    origins = [
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://localhost:3000",
        "http://127.0.0.1:3000",
        "http://localhost:5174",
        "http://127.0.0.1:5174",
        "http://10.0.2.2:8000",   # Standard Android Emulator host loopback
        "*"                       # Allow LAN / mobile access during development
    ]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins if "*" not in origins else ["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount Static Directories for Generated Documents and Audio
STATIC_DIR = BACKEND_ROOT / "static"
STATIC_DIR.mkdir(parents=True, exist_ok=True)
OUTPUT_DIR = STATIC_DIR / "outputs"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
AUDIO_DIR = BACKEND_ROOT / "data" / "audio"
AUDIO_DIR.mkdir(parents=True, exist_ok=True)

app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")

# Include Core Offline Routers
app.include_router(constitution_router)
app.include_router(voice_router)

# Integrate Existing KanoonAI Routers if available
try:
    from app.api import (
        chatbot_routes,
        document_routes,
        summarizer_routes,
        analyzer_routes,
        notice_routes,
        learning_routes,
        faq_routes,
        auth_routes,
        vani_routes
    )
    app.include_router(chatbot_routes.router)
    app.include_router(document_routes.router)
    app.include_router(summarizer_routes.router)
    app.include_router(analyzer_routes.router)
    app.include_router(notice_routes.router)
    app.include_router(learning_routes.router)
    app.include_router(faq_routes.router)
    app.include_router(auth_routes.router)
    app.include_router(vani_routes.router)
    print("[Vani-Kanoon] Successfully loaded legacy application routers.")
except Exception as e:
    print(f"[Vani-Kanoon] Note on legacy router integration: {e}")


@app.on_event("startup")
async def startup_event():
    """
    Validates on-device Constitution index on startup.
    Auto-indexes if index file does not yet exist.
    """
    print("\n" + "=" * 60)
    print("  VANI-KANOON OFFLINE LEGAL ENGINE INITIALIZING")
    print(f"  AI Mode: {os.getenv('AI_MODE', 'offline').upper()}")
    print(f"  Ollama URL: {os.getenv('OLLAMA_BASE_URL', 'http://localhost:11434')}")
    print(f"  Model: {os.getenv('OLLAMA_MODEL', 'gemma3:4b')}")
    print("=" * 60)

    from rag.retriever import get_constitution_retriever
    retriever = get_constitution_retriever()
    if not retriever.is_ready():
        print("[Startup] Constitution vector database not loaded. Checking data files...")
        index_file = BACKEND_ROOT / "vectorstore" / "constitution.index"
        data_file = BACKEND_ROOT / "data" / "constitution" / "constitution.txt"
        if not index_file.exists() and data_file.exists():
            print("[Startup] Auto-building initial Constitution index...")
            try:
                from scripts.build_index import build_faiss_index
                build_faiss_index()
                retriever.load()
            except Exception as e:
                print(f"[Startup] Index auto-build note: {e}")


@app.get("/")
async def root():
    return {
        "service": "Vani-Kanoon Legal AI",
        "status": "online",
        "offline_constitution_rag": "enabled",
        "endpoints": {
            "ask": "/api/constitution/ask",
            "status": "/api/constitution/status",
            "articles": "/api/constitution/articles",
            "transcribe": "/api/voice/transcribe",
            "synthesize": "/api/voice/synthesize",
            "voice_ask": "/api/voice/ask"
        }
    }


if __name__ == "__main__":
    import uvicorn
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", "8000"))
    print(f"Starting Vani-Kanoon Server on http://{host}:{port}")
    uvicorn.run("main:app", host=host, port=port, reload=True)
