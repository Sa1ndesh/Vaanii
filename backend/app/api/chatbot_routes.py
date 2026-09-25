from typing import Optional
from fastapi import APIRouter, HTTPException, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from pydantic import BaseModel
import re
from app.services.ollama_client import chat
from app.core.security import get_current_user, decode_access_token

# Optional auth: returns user payload or None (does not raise)
security_bearer_optional = HTTPBearer(auto_error=False)


def get_current_user_optional(
    auth: Optional[HTTPAuthorizationCredentials] = Depends(security_bearer_optional)
) -> Optional[dict]:
    """Returns user payload if valid token provided, otherwise None."""
    if not auth or not auth.credentials:
        return None
    payload = decode_access_token(auth.credentials)
    if not payload or "sub" not in payload:
        return None
    return payload

# --- REVISED SYSTEM PROMPT (VERSION 5) ---
SYSTEM_PROMPT = """
You are **KanoonAI**, an advanced AI Assistant. Your primary goal is to provide accurate, reliable, and helpful legal information based on the **Indian legal system**. Your internal knowledge is up-to-date as of late 2023.

Follow these principles in every response:

1.  **Persona:** You are a professional, polite, and deeply knowledgeable assistant. Your tone should be formal but approachable.
2.  **Core Task (Legal):** Provide factually correct legal information based on authoritative Indian laws (IPC, CrPC, RERA, IT Act, Consumer Protection Act, etc.).
3.  **Core Task (Factual):** For general knowledge questions (e.g., "who is the chief justice"), answer based on your internal knowledge.
4.  **Clarity and Structure:** Deliver answers in a clear, organized manner.
5.  **Legal Citations:** When answering a *legal* query, support your answer by citing the relevant legal sections or acts (e.g., *"This matter is covered under Section 420 of the Indian Penal Code (IPC)"*).
6.  **Handling Vague Queries:** If a user's query is vague or lacks context (e.g., "are you sure?"), you MUST ask for clarification politely. (e.g., "I'm not sure I follow. Could you please provide more context?").
7.  **Relevant Law Field:** You MUST include a special line in your response formatted exactly like this: `Relevant Law: [Your Answer]`
    * If the query is legal, fill `[Your Answer]` with the cited law. (e.g., `Relevant Law: IPC Section 499`)
    * If the query is factual or non-legal, fill `[Your Answer]` with "Factual Inquiry". (e.g., `Relevant Law: Factual Inquiry`)
8.  **Formatting: You MUST respond in plain text only. Do NOT use Markdown, asterisks (*), bolding (**), quotes ("), or any other special formatting.** Your output must be clean text.
"""
# --- END OF REVISED PROMPT ---

# This is the disclaimer our Python code will add
LEGAL_DISCLAIMER = (
    "\n\nDisclaimer: I am an AI assistant and the information provided is for "
    "educational and informational purposes only. It should not be considered a "
    "substitute for professional legal advice. Please consult a qualified lawyer "
    "for advice on your specific situation."
)

# --- API Configuration ---
router = APIRouter(
    prefix="/api/v1/chatbot",
    tags=["Chatbot"]
)

class ChatQuery(BaseModel):
    query: str

@router.post("/query")
async def handle_chat_query(
    chat_query: ChatQuery,
    current_user: Optional[dict] = Depends(get_current_user_optional)
):
    """
    Handles a user's legal query by sending it to the Ollama model.
    Authentication is optional - unauthenticated users can still use basic features.
    """
    try:
        ai_text = await chat(
            prompt=chat_query.query,
            system=SYSTEM_PROMPT,
            temperature=0.5,
            max_tokens=1024
        )

        relevant_law = "See response"  # Default
        law_match = re.search(r"Relevant Law: (.*)", ai_text, re.IGNORECASE)
        
        if law_match:
            relevant_law = law_match.group(1).strip()
            ai_text = re.sub(r"Relevant Law: (.*)", "", ai_text, flags=re.IGNORECASE).strip()

        if relevant_law.lower() != "factual inquiry":
            ai_text += LEGAL_DISCLAIMER
        
        return {
            "user_query": chat_query.query,
            "ai_response": ai_text,
            "relevant_law": relevant_law
        }
        
    except Exception as e:
        error_message = str(e)
        print(f"An error occurred: {error_message}")
        raise HTTPException(status_code=500, detail=f"Error communicating with AI: {error_message}")