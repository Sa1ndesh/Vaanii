from app.services.ollama_client import chat

# --- Configuration ---

SYSTEM_INSTRUCTION = """You are an expert AI legal assistant specializing in Indian law.
Your role is to provide clear, informative, and neutral explanations to a layperson.
1.  **Analyze the User's Query:** Understand the core legal question.
2.  **Provide Relevant Law:** Identify the primary act, section, or legal principle involved.
3.  **Explain Simply:** Explain the law in simple, non-jargon terms.
4.  **Suggest Next Steps:** Provide practical general-purpose next steps.
5.  **Disclaimer:** ALWAYS end with: "Please note: I am an AI assistant and this is not legal advice. Consult a qualified legal professional for advice specific to your situation."
6.  **Tone:** Be professional, empathetic, and helpful. Keep responses concise.
"""

# --- Service Logic ---

async def get_chatbot_response(user_query: str, chat_history: list[dict]):
    """
    Calls the Ollama local model with the user's query and chat history.
    chat_history is expected in Gemini format: [{"role": "user"/"model", "parts": [{"text": "..."}]}]
    We convert it to Ollama format: [{"role": "user"/"assistant", "content": "..."}]
    """
    # Convert history from Gemini format → Ollama format
    ollama_history = []
    for msg in chat_history:
        role = "assistant" if msg.get("role") == "model" else msg.get("role", "user")
        parts = msg.get("parts", [])
        content = parts[0].get("text", "") if parts else ""
        if content:
            ollama_history.append({"role": role, "content": content})

    try:
        reply = await chat(
            user_query,
            system=SYSTEM_INSTRUCTION,
            history=ollama_history,
            temperature=0.3,
            max_tokens=1024,
        )
        return {"text": reply, "sources": []}

    except Exception as e:
        print(f"[chatbot_service] Error: {e}")
        return {"error": f"An unexpected error occurred: {str(e)}"}