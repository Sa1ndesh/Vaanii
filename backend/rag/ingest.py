"""
Constitution Ingestion and Semantic Chunking Pipeline.
Parses the Constitution of India into structured chunks preserving
Parts, Chapters, Articles, Clauses, and Schedules.
"""

import json
import re
from pathlib import Path
from typing import List, Optional
from utils.text_cleaner import clean_text
from utils.metadata import ConstitutionChunk


def parse_constitution_text(raw_text: str) -> List[ConstitutionChunk]:
    """
    Parses cleaned Constitution text into hierarchical, semantic chunks.
    Identifies Preamble, Parts, Articles, and Schedules with strict legal metadata.
    """
    cleaned = clean_text(raw_text)
    chunks: List[ConstitutionChunk] = []

    # 1. Preamble Extraction
    preamble_match = re.search(r"PREAMBLE\s*\n(.*?)(?=\nPART\s+I|\Z)", cleaned, re.DOTALL | re.IGNORECASE)
    if preamble_match:
        content = preamble_match.group(1).strip()
        chunks.append(ConstitutionChunk(
            chunk_id="preamble",
            part=None,
            chapter=None,
            article="Preamble",
            clause=None,
            schedule=None,
            title="Preamble to the Constitution of India",
            content=f"PREAMBLE:\n{content}",
            source="Constitution of India, Preamble",
            keywords=["preamble", "sovereign", "socialist", "secular", "democratic", "republic", "justice", "liberty", "equality", "fraternity"]
        ))

    # 2. Iterate through Parts and Articles
    # Split text into major sections by "ARTICLE " and "SCHEDULE"
    lines = cleaned.split("\n")
    current_part: Optional[str] = None
    current_chapter: Optional[str] = None
    
    current_article_id: Optional[str] = None
    current_title: str = ""
    current_content_lines: List[str] = []
    
    def flush_current_article():
        if current_article_id and current_content_lines:
            text_body = "\n".join(current_content_lines).strip()
            # Extract keywords
            extracted_keywords = [
                current_article_id.lower(),
                current_title.lower(),
            ]
            if "fundamental rights" in (current_part or "").lower() or int(re.sub(r"\D", "", current_article_id) or 0) in range(12, 36):
                extracted_keywords.append("fundamental rights")
            if "duty" in current_title.lower() or current_article_id == "Article 51A":
                extracted_keywords.extend(["fundamental duties", "duties", "51a", "eleven duties"])
            if current_article_id in ["Article 14", "Article 15", "Article 16", "Article 17", "Article 18"]:
                extracted_keywords.append("right to equality")
            if current_article_id in ["Article 19", "Article 20", "Article 21", "Article 21A", "Article 22"]:
                extracted_keywords.extend(["right to freedom", "liberty", "personal liberty"])
            if current_article_id == "Article 21":
                extracted_keywords.extend(["life and personal liberty", "privacy", "puttaswamy", "maneka gandhi", "dignity", "clean environment"])
            if current_article_id == "Article 32":
                extracted_keywords.extend(["constitutional remedies", "writs", "habeas corpus", "mandamus", "certiorari", "prohibition", "quo warranto", "heart and soul"])
            if current_article_id == "Article 226":
                extracted_keywords.extend(["high court writs", "habeas corpus", "mandamus", "certiorari", "prohibition", "quo warranto"])

            chunks.append(ConstitutionChunk(
                chunk_id=f"art_{current_article_id.lower().replace(' ', '_')}",
                part=current_part,
                chapter=current_chapter,
                article=current_article_id,
                clause=None,
                schedule=None,
                title=current_title or current_article_id,
                content=f"{current_article_id}: {current_title}\n{text_body}",
                source=f"Constitution of India, {current_part or ''}, {current_article_id}".strip(", "),
                keywords=list(set(extracted_keywords))
            ))

    in_schedules_section = False
    current_schedule_id: Optional[str] = None
    current_schedule_lines: List[str] = []

    def flush_current_schedule():
        if current_schedule_id and current_schedule_lines:
            sched_body = "\n".join(current_schedule_lines).strip()
            chunks.append(ConstitutionChunk(
                chunk_id=f"sched_{current_schedule_id.lower().replace(' ', '_')}",
                part=None,
                chapter=None,
                article=None,
                clause=None,
                schedule=current_schedule_id,
                title=f"{current_schedule_id} to the Constitution of India",
                content=f"{current_schedule_id}:\n{sched_body}",
                source=f"Constitution of India, {current_schedule_id}",
                keywords=[current_schedule_id.lower(), "schedules", "schedule", "constitution of india"]
            ))

    for line in lines:
        stripped = line.strip()
        if not stripped:
            if current_content_lines:
                current_content_lines.append("")
            if current_schedule_lines:
                current_schedule_lines.append("")
            continue

        # Detect PART
        part_match = re.match(r"^(PART\s+[IVXLCDM]+(?:[A-Z])?)(?::\s*(.*))?$", stripped, re.IGNORECASE)
        if part_match:
            flush_current_article()
            current_part = stripped
            current_chapter = None
            current_article_id = None
            current_content_lines = []
            continue

        # Detect SCHEDULE section header
        if stripped == "SCHEDULES TO THE CONSTITUTION OF INDIA":
            flush_current_article()
            in_schedules_section = True
            current_article_id = None
            continue

        if in_schedules_section:
            sched_header_match = re.match(r"^((?:FIRST|SECOND|THIRD|FOURTH|FIFTH|SIXTH|SEVENTH|EIGHTH|NINTH|TENTH|ELEVENTH|TWELFTH)\s+SCHEDULE)(?::\s*(.*))?$", stripped, re.IGNORECASE)
            if sched_header_match:
                flush_current_schedule()
                current_schedule_id = sched_header_match.group(1).title()
                current_schedule_lines = [sched_header_match.group(2) or ""]
            else:
                if current_schedule_id:
                    current_schedule_lines.append(stripped)
            continue

        # Detect ARTICLE
        art_match = re.match(r"^ARTICLE\s+([0-9]+[A-Z]?)(?::\s*(.*))?$", stripped, re.IGNORECASE)
        if art_match:
            flush_current_article()
            current_article_id = f"Article {art_match.group(1).upper()}"
            current_title = (art_match.group(2) or "").strip()
            current_content_lines = []
            continue

        # Accumulate lines
        if current_article_id:
            current_content_lines.append(stripped)

    # Flush last buffers
    flush_current_article()
    flush_current_schedule()

    return chunks


def ingest_constitution(
    input_file_path: Path,
    output_chunks_path: Path
) -> List[ConstitutionChunk]:
    """
    Ingests raw Constitution text, chunks it, and writes JSON to output_chunks_path.
    """
    if not input_file_path.exists():
        raise FileNotFoundError(f"Constitution source file not found at: {input_file_path}")

    raw_text = input_file_path.read_text(encoding="utf-8")
    chunks = parse_constitution_text(raw_text)

    output_chunks_path.parent.mkdir(parents=True, exist_ok=True)
    chunks_data = [chunk.model_dump() for chunk in chunks]
    output_chunks_path.write_text(json.dumps(chunks_data, indent=2, ensure_ascii=False), encoding="utf-8")

    return chunks


if __name__ == "__main__":
    base_dir = Path(__file__).resolve().parent.parent
    src = base_dir / "data" / "constitution" / "constitution.txt"
    dst = base_dir / "data" / "processed" / "chunks.json"
    result = ingest_constitution(src, dst)
    print(f"Successfully parsed and ingested {len(result)} Constitution chunks into {dst}")
