"""
FastAPI Endpoints for Offline Constitution Assistant.
Implements:
- POST /api/constitution/ask
- GET  /api/constitution/status
- GET  /api/constitution/articles
"""

import os
from pathlib import Path
from typing import List, Optional
from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel

from utils.metadata import (
    ConstitutionQueryRequest,
    ConstitutionQueryResponse,
    SystemStatusResponse,
    SourceReference
)
from rag.retriever import get_constitution_retriever
from rag.generator import get_generator

router = APIRouter(prefix="/api/constitution", tags=["Offline Constitution Assistant"])


@router.post("/ask", response_model=ConstitutionQueryResponse)
async def ask_constitution(request: ConstitutionQueryRequest):
    """
    Asks a question about the Constitution of India.
    Performs local vector retrieval + grounded local LLM synthesis.
    Works 100% offline without Gemini, Google Cloud, or internet connectivity.
    """
    q = request.question.strip()
    if not q:
        raise HTTPException(status_code=400, detail="Question cannot be empty.")

    try:
        retriever = get_constitution_retriever()
        retrieved_chunks = retriever.retrieve(q, top_k=request.top_k)
    except FileNotFoundError as e:
        raise HTTPException(status_code=503, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Retrieval error: {str(e)}")

    generator = get_generator()
    answer_text, model_name, is_offline = generator.generate(
        question=q,
        retrieved_chunks=retrieved_chunks,
        language=request.language or "en"
    )

    sources = [
        SourceReference(
            article=c.article or c.schedule or "Constitutional Provision",
            title=c.title,
            content=c.content
        )
        for c in retrieved_chunks
    ]

    return ConstitutionQueryResponse(
        answer=answer_text,
        sources=sources,
        offline=is_offline,
        model_used=model_name,
        retrieval_count=len(retrieved_chunks)
    )


@router.get("/status", response_model=SystemStatusResponse)
async def get_constitution_status():
    """
    Reports the health and availability of all offline Constitution RAG subsystems:
    - Constitution database
    - FAISS vector index
    - Local embedding model
    - Local Ollama LLM
    """
    base_dir = Path(__file__).resolve().parent.parent
    data_file = base_dir / "data" / "constitution" / "constitution.txt"
    index_file = base_dir / "vectorstore" / "constitution.index"
    meta_file = base_dir / "vectorstore" / "metadata.json"

    retriever = get_constitution_retriever()
    generator = get_generator()
    ollama_ok, ollama_msg, available_models = generator.check_ollama_status()

    chunks_count = len(retriever._chunks) if retriever.is_ready() else 0

    return SystemStatusResponse(
        constitution_data_available=data_file.exists(),
        faiss_index_available=index_file.exists() or meta_file.exists(),
        embedding_model_available=True,
        ollama_available=ollama_ok,
        selected_llm_model=generator.ollama_model,
        offline_mode_active=generator.ai_mode == "offline",
        total_indexed_chunks=chunks_count,
        data_path=str(data_file),
        index_path=str(index_file),
        device="cpu (Local On-Device)"
    )


@router.get("/articles")
async def list_constitution_articles(limit: int = Query(50, ge=1, le=200)):
    """
    Returns index of all available constitutional provisions in the local database.
    """
    retriever = get_constitution_retriever()
    if not retriever.is_ready():
        retriever.load()

    items = []
    for c in retriever._chunks[:limit]:
        items.append({
            "chunk_id": c.chunk_id,
            "article": c.article or c.schedule,
            "part": c.part,
            "title": c.title,
            "preview": c.content[:160] + ("..." if len(c.content) > 160 else "")
        })
    return {"total": len(retriever._chunks), "articles": items}
