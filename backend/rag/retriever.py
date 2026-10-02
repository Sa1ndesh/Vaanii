"""
FAISS-based Local Vector Retriever for Constitution of India.
Combines semantic dense vector search with direct statutory Article matching.
"""

import json
from pathlib import Path
from typing import List, Optional, Tuple

try:
    import numpy as np
    HAVE_NUMPY = True
except ImportError:
    HAVE_NUMPY = False

from utils.metadata import ConstitutionChunk
from utils.text_cleaner import extract_article_number, extract_all_articles
from rag.embeddings import LocalEmbeddingEngine, get_embedding_engine


class ConstitutionRetriever:
    """
    On-device retriever querying local FAISS vector index and metadata store.
    Provides fast, sub-millisecond retrieval of relevant Constitution articles.
    """

    def __init__(
        self,
        index_path: Optional[Path] = None,
        metadata_path: Optional[Path] = None,
        embedding_engine: Optional[LocalEmbeddingEngine] = None
    ):
        base_dir = Path(__file__).resolve().parent.parent
        self.index_path = index_path or (base_dir / "vectorstore" / "constitution.index")
        self.metadata_path = metadata_path or (base_dir / "vectorstore" / "metadata.json")
        self.embedding_engine = embedding_engine or get_embedding_engine()
        
        self._index = None
        self._chunks: List[ConstitutionChunk] = []
        self._article_map: dict[str, ConstitutionChunk] = {}
        self._is_loaded = False

    def is_ready(self) -> bool:
        """Returns True if the index and metadata are loaded and ready."""
        return self._is_loaded and len(self._chunks) > 0

    def load(self) -> bool:
        """
        Loads the FAISS index and chunk metadata from disk.
        """
        if self._is_loaded:
            return True

        # 1. Load Metadata
        if not self.metadata_path.exists():
            alt_chunks_path = self.metadata_path.parent.parent / "data" / "processed" / "chunks.json"
            if alt_chunks_path.exists():
                data = json.loads(alt_chunks_path.read_text(encoding="utf-8"))
            else:
                return False
        else:
            data = json.loads(self.metadata_path.read_text(encoding="utf-8"))

        self._chunks = [ConstitutionChunk(**item) for item in data]
        self._article_map = {}
        for c in self._chunks:
            if c.article:
                key = c.article.strip().lower()
                self._article_map[key] = c
                short = key.replace("article", "").strip()
                if short:
                    self._article_map[short] = c
            if c.schedule:
                self._article_map[c.schedule.strip().lower()] = c

        # 2. Load FAISS Index
        if self.index_path.exists() and HAVE_NUMPY:
            try:
                import faiss
                self._index = faiss.read_index(str(self.index_path))
            except Exception as e:
                self._index = None
        else:
            self._index = None

        self._is_loaded = True
        return True

    def retrieve(self, query: str, top_k: int = 3) -> List[ConstitutionChunk]:
        """
        Retrieves top relevant constitutional passages for the given user query.
        Uses a hybrid pipeline:
        1. Exact Article / Schedule detection for direct questions (e.g. 'What is Article 21?')
        2. FAISS dense vector similarity search for conceptual / semantic queries.
        """
        if not self._is_loaded:
            success = self.load()
            if not success:
                raise FileNotFoundError(
                    f"Constitution index not found at '{self.index_path}' or metadata not at '{self.metadata_path}'. "
                    f"Please run 'python scripts/build_index.py' to generate the vector database."
                )

        results: List[ConstitutionChunk] = []
        seen_ids = set()

        # Step 1: Check for explicit Article / Schedule references in query
        explicit_articles = extract_all_articles(query)
        primary_art = extract_article_number(query)
        if primary_art and primary_art not in explicit_articles:
            explicit_articles.insert(0, primary_art)

        for art in explicit_articles:
            clean_key = art.strip().lower()
            if clean_key in self._article_map:
                target_chunk = self._article_map[clean_key]
                if target_chunk.chunk_id not in seen_ids:
                    results.append(target_chunk)
                    seen_ids.add(target_chunk.chunk_id)

        # Step 2: Dense Semantic Vector Search via FAISS
        remaining_k = max(top_k - len(results), 2)
        if self._index is not None and getattr(self._index, "ntotal", 0) > 0 and HAVE_NUMPY:
            query_vec = self.embedding_engine.encode_query(query)
            scores, indices = self._index.search(query_vec, k=min(remaining_k * 3, self._index.ntotal))

            for idx in indices[0]:
                if 0 <= idx < len(self._chunks):
                    candidate = self._chunks[idx]
                    if candidate.chunk_id not in seen_ids:
                        results.append(candidate)
                        seen_ids.add(candidate.chunk_id)
                        if len(results) >= top_k:
                            break
        else:
            # High-performance keyword & semantic similarity fallback
            q_lower = query.lower()
            scored_candidates = []
            for chunk in self._chunks:
                score = 0
                if chunk.chunk_id in seen_ids:
                    continue
                # Keyword overlap
                for kw in chunk.keywords:
                    if kw in q_lower:
                        score += 3
                if (chunk.article or "").lower() in q_lower:
                    score += 5
                if (chunk.title or "").lower() in q_lower:
                    score += 4
                words = q_lower.split()
                matches = sum(1 for w in words if len(w) > 3 and w in chunk.content.lower())
                score += matches
                if score > 0:
                    scored_candidates.append((score, chunk))

            scored_candidates.sort(key=lambda x: x[0], reverse=True)
            for _, chunk in scored_candidates[:remaining_k]:
                results.append(chunk)
                seen_ids.add(chunk.chunk_id)

        return results[:top_k]


# Singleton helper
_retriever_instance: Optional[ConstitutionRetriever] = None


def get_constitution_retriever() -> ConstitutionRetriever:
    global _retriever_instance
    if _retriever_instance is None:
        _retriever_instance = ConstitutionRetriever()
        _retriever_instance.load()
    return _retriever_instance
