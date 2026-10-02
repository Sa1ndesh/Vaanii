"""
Data models and metadata structures for the Indian Constitution RAG pipeline.
Supports both Pydantic (v1/v2) and standard Python dataclasses for maximum compatibility.
"""

from typing import List, Optional, Dict, Any

try:
    from pydantic import BaseModel, Field

    class ConstitutionChunk(BaseModel):
        chunk_id: str
        part: Optional[str] = None
        chapter: Optional[str] = None
        article: Optional[str] = None
        clause: Optional[str] = None
        schedule: Optional[str] = None
        title: str
        content: str
        source: str = "Constitution of India"
        keywords: List[str] = []

    class SourceReference(BaseModel):
        article: str
        title: str
        content: str

    class ConstitutionQueryRequest(BaseModel):
        question: str
        top_k: int = 3
        language: Optional[str] = "en"

    class ConstitutionQueryResponse(BaseModel):
        answer: str
        sources: List[SourceReference] = []
        offline: bool = True
        model_used: Optional[str] = None
        retrieval_count: int = 0

    class SystemStatusResponse(BaseModel):
        constitution_data_available: bool
        faiss_index_available: bool
        embedding_model_available: bool
        ollama_available: bool
        selected_llm_model: str
        offline_mode_active: bool
        total_indexed_chunks: int
        data_path: str
        index_path: str
        device: str

except ImportError:
    from dataclasses import dataclass, field, asdict

    @dataclass
    class ConstitutionChunk:
        chunk_id: str
        title: str
        content: str
        part: Optional[str] = None
        chapter: Optional[str] = None
        article: Optional[str] = None
        clause: Optional[str] = None
        schedule: Optional[str] = None
        source: str = "Constitution of India"
        keywords: List[str] = field(default_factory=list)

        def model_dump(self) -> Dict[str, Any]:
            return asdict(self)

    @dataclass
    class SourceReference:
        article: str
        title: str
        content: str

        def model_dump(self) -> Dict[str, Any]:
            return asdict(self)

    @dataclass
    class ConstitutionQueryRequest:
        question: str
        top_k: int = 3
        language: Optional[str] = "en"

    @dataclass
    class ConstitutionQueryResponse:
        answer: str
        sources: List[SourceReference] = field(default_factory=list)
        offline: bool = True
        model_used: Optional[str] = None
        retrieval_count: int = 0

    @dataclass
    class SystemStatusResponse:
        constitution_data_available: bool
        faiss_index_available: bool
        embedding_model_available: bool
        ollama_available: bool
        selected_llm_model: str
        offline_mode_active: bool
        total_indexed_chunks: int
        data_path: str
        index_path: str
        device: str
