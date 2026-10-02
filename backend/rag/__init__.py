"""
Vani-Kanoon Local RAG Engine for the Constitution of India.
"""

from rag.ingest import ingest_constitution, parse_constitution_text
from rag.embeddings import LocalEmbeddingEngine, get_embedding_engine
from rag.retriever import ConstitutionRetriever, get_constitution_retriever
from rag.prompt import build_rag_prompt, CONSTITUTION_SYSTEM_PROMPT
from rag.generator import LocalGenerator, get_generator

__all__ = [
    "ingest_constitution",
    "parse_constitution_text",
    "LocalEmbeddingEngine",
    "get_embedding_engine",
    "ConstitutionRetriever",
    "get_constitution_retriever",
    "build_rag_prompt",
    "CONSTITUTION_SYSTEM_PROMPT",
    "LocalGenerator",
    "get_generator"
]
