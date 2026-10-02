"""
Text cleaning and normalization utilities for Constitution of India documents.
Cleans raw text, strips noise, and standardizes Article identifiers.
"""

import re
from typing import Optional, Tuple


def clean_text(text: str) -> str:
    """
    Cleans raw text extracted from Constitution files.
    Removes page numbers, recurring headers/footers, and excessive whitespace.
    """
    if not text:
        return ""

    # Remove BOM and non-printable characters
    cleaned = text.replace("\ufeff", "").replace("\r\n", "\n").replace("\r", "\n")

    # Remove typical Constitution PDF/Scrape running headers and footers
    cleaned = re.sub(r"(?i)THE CONSTITUTION OF INDIA\s*(\([^\)]*\))?", "", cleaned)
    cleaned = re.sub(r"(?i)Page\s+\d+\s+of\s+\d+", "", cleaned)
    cleaned = re.sub(r"\n\s*\d+\s*\n", "\n", cleaned)  # Standalone page numbers

    # Normalize unicode quotes and dashes
    cleaned = cleaned.replace("“", '"').replace("”", '"').replace("’", "'").replace("‘", "'")
    cleaned = cleaned.replace("—", " - ").replace("–", " - ")

    # Collapse multiple spaces while preserving paragraphs
    lines = [re.sub(r"[ \t]+", " ", line).strip() for line in cleaned.split("\n")]
    
    # Remove empty line runs longer than 2
    result_lines = []
    empty_count = 0
    for line in lines:
        if not line:
            empty_count += 1
            if empty_count <= 2:
                result_lines.append("")
        else:
            empty_count = 0
            result_lines.append(line)

    return "\n".join(result_lines).strip()


def extract_article_number(query_or_title: str) -> Optional[str]:
    """
    Extracts standardized Article identifier from user queries or chunk headers.
    Examples:
        'What is Article 21?' -> 'Article 21'
        'explain art 14' -> 'Article 14'
        'Article 51A' -> 'Article 51A'
        'difference between Article 14 and Article 21' -> 'Article 14' (primary)
    """
    if not query_or_title:
        return None

    # Matches Article 21, Art. 21, Art 21, Article 51A, Article 300A, etc.
    match = re.search(r"\b(?:article|art\.?)\s*([0-9]+[a-z]?)\b", query_or_title, re.IGNORECASE)
    if match:
        num = match.group(1).upper()
        return f"Article {num}"
    
    # Matches Preamble
    if re.search(r"\bpreamble\b", query_or_title, re.IGNORECASE):
        return "Preamble"

    # Matches Schedule (e.g. 7th Schedule, Schedule 8)
    sched_match = re.search(r"\b(?:([0-9]+)(?:st|nd|rd|th)?\s+schedule|schedule\s+([0-9]+))\b", query_or_title, re.IGNORECASE)
    if sched_match:
        s_num = sched_match.group(1) or sched_match.group(2)
        return f"Schedule {s_num}"

    return None


def extract_all_articles(text: str) -> list[str]:
    """
    Finds all referenced Article numbers within a text.
    """
    matches = re.findall(r"\b(?:article|art\.?)\s*([0-9]+[a-z]?)\b", text, re.IGNORECASE)
    return [f"Article {m.upper()}" for m in matches]


def normalize_whitespace(text: str) -> str:
    """Replaces multiple whitespace characters with a single space."""
    return re.sub(r"\s+", " ", text).strip()
