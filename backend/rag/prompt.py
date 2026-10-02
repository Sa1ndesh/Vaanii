"""
Prompt engineering and template management for Constitution RAG.
Enforces strict grounding, citation rules, and anti-hallucination constraints.
"""

from typing import List
from utils.metadata import ConstitutionChunk

CONSTITUTION_SYSTEM_PROMPT = """You are 'Vani-Kanoon Offline Constitution Assistant', an authoritative, trustworthy Indian constitutional scholar and legal intelligence assistant running completely on-device without Internet.

MANDATORY RULES & CONSTRAINTS:
1. Grounding: Answer strictly using the retrieved Constitutional context provided below.
2. Anti-Hallucination: Do NOT invent Articles, Amendments, clauses, or case laws not present in the context or Indian Constitution.
3. Insufficiency: If the retrieved information does not contain enough context to answer the user's specific query, explicitly state: "The requested specific provision is not available in the offline Constitution knowledge base."
4. Citations: Always include exact Article numbers (e.g. Article 14, Article 21, Article 32, Article 51A) or Schedule numbers whenever applicable.
5. Structure:
   - Provide a clear, direct answer in simple words.
   - Distinguish the verbatim constitutional provision from practical explanation.
   - Mention the core rights, safeguards, or duties enshrined.
6. Legal Disclaimer: Do not claim that this answer constitutes formal advocate-client legal advice. Keep it educational and statutory.
7. Voice & Conciseness: Keep sentences natural, clear, and easy to read aloud by Text-To-Speech. Avoid unnecessary markdown tables or obscure symbols.
8. Scope: For queries completely outside the Constitution of India (e.g. general chit-chat or non-constitutional matters), politely clarify that this offline module specializes in the Constitution of India.
"""


def build_rag_prompt(question: str, retrieved_chunks: List[ConstitutionChunk], language: str = "en") -> str:
    """
    Constructs the final grounded prompt incorporating retrieved constitutional provisions.
    """
    context_blocks = []
    for idx, chunk in enumerate(retrieved_chunks, start=1):
        heading = f"[{idx}] {chunk.article or chunk.schedule or 'PROVISION'}: {chunk.title}"
        if chunk.part:
            heading += f" ({chunk.part})"
        body = chunk.content.strip()
        context_blocks.append(f"{heading}\n{body}")

    context_str = "\n\n".join(context_blocks)

    lang_instruction = ""
    if language and language.lower().startswith("hi"):
        lang_instruction = "\nIMPORTANT: Please explain and answer fluently in Hindi (हिन्दी) using clear constitutional terminology."

    prompt = f"""RETRIEVED CONSTITUTIONAL PASSAGES:
----------------------------------------
{context_str}
----------------------------------------

USER QUESTION: {question}
{lang_instruction}

ANSWER (grounded strictly in the retrieved Constitution text above):"""

    return prompt
