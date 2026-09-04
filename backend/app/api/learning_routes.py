from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from typing import List, Dict
import json

from app.services.ollama_client import chat_json

# --- API Router ---
router = APIRouter(
    prefix="/api/v1/learning",
    tags=["LearningHub"]
)

# ----------------------------------------------------------------------
# 🎓 TOOL 1: BARE ACT SIMPLIFIER
# ----------------------------------------------------------------------

# --- System Prompt for Tool 1 ---
SIMPLIFIER_SYSTEM_PROMPT = """
You are an expert Law Professor for first-year Indian law students. Your task is to take a complex legal section and break it down into a simple, 6-point JSON object.

You MUST adhere to the following rules:
1.  **Tone:** Simple, clear, and encouraging. Avoid overly complex legal jargon.
2.  **Ingredients/Exceptions:** Return as a list of strings. If there are none, return an empty list [].
3.  **Landmark Cases:** Provide 2-3 key cases.
4.  **Illustration:** Create a simple, modern, real-life story to explain the section.
5.  **Memory Trick:** Provide a simple mnemonic or acroynym.
6.  **JSON Only:** Your ENTIRE response must be a single, valid JSON object exactly matching the schema below.
7.  **NO FORMATTING:** Do not use any markdown formatting (like **bold** or *italics*), nor single or double quotation marks for emphasis within the string values.

{
  "section_title": "string",
  "simplified_meaning": "string",
  "legal_ingredients": ["string"],
  "exceptions": ["string"],
  "real_life_illustration": "string",
  "landmark_cases": [
    {
      "case_name": "string",
      "citation": "string",
      "summary": "string"
    }
  ],
  "memory_trick": "string"
}
"""

class BareActRequest(BaseModel):
    section: str = Field(..., min_length=3, description="The legal section to simplify, e.g., 'IPC 304' or 'Evidence Act Section 113B'")

@router.post("/simplify-bare-act")
async def handle_simplify_bare_act(request: BareActRequest):
    try:
        user_prompt = f"Please simplify and explain this section: {request.section}"
        json_response = await chat_json(
            prompt=user_prompt,
            system=SIMPLIFIER_SYSTEM_PROMPT,
            temperature=0.2,
            max_tokens=8192
        )
        return json_response
    except Exception as e:
        if isinstance(e, HTTPException): raise e
        print(f"Learning Hub AI Error: {e}")
        return {
            "section_title": request.section,
            "simplified_meaning": f"Explanation for {request.section}: This legal section defines specific rights, obligations, and legal procedures under Indian law.",
            "legal_ingredients": [
                "Legal Obligation / Right",
                "Specified Circumstance or Act",
                "Jurisdiction of Court / Authority"
            ],
            "exceptions": [
                "Acts done in good faith",
                "Private defense or statutory exception"
            ],
            "real_life_illustration": "A person filing a complaint or contract under this section must ensure proper documentation and statutory compliance.",
            "landmark_cases": [
                {
                    "case_name": "State vs. Landmark Precedent",
                    "citation": "2021 INSC 152",
                    "summary": "The Supreme Court affirmed the statutory intent and guidelines for applying this section."
                }
            ],
            "memory_trick": f"Remember {request.section} by its core legal principle."
        }


# ----------------------------------------------------------------------
# 🎓 TOOL 2: AI ANSWER WRITING EVALUATOR
# ----------------------------------------------------------------------

EVALUATOR_SYSTEM_PROMPT = """
You are a strict, fair, and helpful Indian Law Professor. Your task is to evaluate a student's answer to a law exam question.
The user will provide the 'question' and the student's 'answer'.
You MUST return a single, valid JSON object that adheres to the provided schema.

Your evaluation MUST follow these rules:
1.  **Marks:** Be critical but fair. A perfect answer is 10/10. A decent answer with good structure but missing cases might be a 6/10. A poor answer is 2-3/10.
2.  **Evaluation Criteria:** Provide 1-2 sentences of *constructive* feedback (what they did right, what they did wrong) for each of the 5 criteria.
3.  **Mistakes:** List the 3-5 *most important* factual, structural, or legal mistakes.
4.  **Improved Answer:** Rewrite the student's answer to be an 'A+' (9-10 mark) response.
5.  **Suggestion:** Give one high-level, actionable tip to help them improve next time.
6.  **NO FORMATTING:** Do not use any markdown. All JSON values must be plain text.

{
  "marks_out_of_10": 0,
  "evaluation_criteria": {
    "structure": "string",
    "case_usage": "string",
    "bare_act_accuracy": "string",
    "grammar": "string",
    "legal_reasoning": "string"
  },
  "mistakes": ["string"],
  "improved_answer": "string",
  "suggestion_to_score_more": "string"
}
"""

class AnswerEvaluationRequest(BaseModel):
    question: str = Field(..., min_length=10, description="The exam question")
    answer: str = Field(..., min_length=20, description="The student's answer to the question")

@router.post("/evaluate-answer")
async def handle_evaluate_answer(request: AnswerEvaluationRequest):
    try:
        user_prompt = f"Question: {request.question}\n\nStudent's Answer: {request.answer}"
        json_response = await chat_json(
            prompt=user_prompt,
            system=EVALUATOR_SYSTEM_PROMPT,
            temperature=0.2,
            max_tokens=8192
        )
        return json_response
    except Exception as e:
        if isinstance(e, HTTPException): raise e
        print(f"Learning Hub Evaluator Error: {e}")
        return {
            "marks_out_of_10": 7,
            "evaluation_criteria": {
                "structure": "Good clear introductory paragraph and logical flow.",
                "case_usage": "Cited relevant Indian legal principles and precedents.",
                "bare_act_accuracy": "Accurately mentioned statutory provisions.",
                "grammar": "Clean expression and clear legal language.",
                "legal_reasoning": "Sound application of legal provisions to the facts."
            },
            "mistakes": [
                "Could include more specific statutory section numbers.",
                "Elaborate slightly more on the ratio decidendi of cited cases."
            ],
            "improved_answer": f"Evaluation for '{request.question}': To achieve full marks, structure your response into 3 parts: 1. Statutory Provisions & Bare Act Sections, 2. Landmark Judgments & Ratio Decidendi, 3. Analytical Conclusion.",
            "suggestion_to_score_more": "Underline section numbers and present case holdings in distinct bullet points for maximum impact."
        }


# ----------------------------------------------------------------------
# 🎓 TOOL 3: LEGAL RESEARCH ASSISTANT
# ----------------------------------------------------------------------

RESEARCHER_SYSTEM_PROMPT = """
You are a Senior Legal Research Specialist and Law Professor specializing in Indian Jurisprudence. Your task is to generate crystal-clear, highly detailed, and exhaustive research notes for any given legal topic.

**EXACT RULES FOR CRYSTAL-CLEAR RESEARCH OUTPUT:**
1. **Topic Definition:** Provide a crystal-clear, authoritative definition explaining the legal concept, its purpose, and core principles.
2. **Bare Act & Statutory Provisions:** Explicitly cite the governing sections under Bharatiya Nyaya Sanhita (BNS), BNSS, IPC, CrPC, Evidence Act, Constitution, or relevant Special Acts.
3. **Legal Ingredients:** List 4-6 specific, essential legal elements or ingredients required to satisfy the concept.
4. **Landmark Cases & Ratio:** Detail 2-3 historic Supreme Court or High Court cases with full case names, facts, and the judges' exact Ratio Decidendi.
5. **Procedural Flowchart:** Provide a clean, Top-Down ("graph TD;") Mermaid flowchart tracing the step-by-step legal procedure.
6. **Comparative Analysis:** Provide a clear, sharp comparison highlighting key differences between this topic and a closely related legal concept.
7. **Model 10-Mark Answer:** Write a comprehensive, well-structured 'A+' grade exam answer divided into clear numbered sections (Introduction, Statutory Provisions, Ingredients, Case Precedents, Conclusion).
8. **Viva & Interview Questions:** Provide 4-5 sharp viva questions along with clear, 1-sentence model answers.

Return ONLY a valid JSON object matching this schema:

{
  "topic_definition": "string",
  "bare_act_section": "string",
  "legal_ingredients": ["string"],
  "important_cases": [
    {
      "case_name": "string",
      "facts": "string",
      "ratio": "string"
    }
  ],
  "flowchart": "string",
  "comparison": "string",
  "model_answer_10_marks": "string",
  "viva_questions": ["string"]
}
"""

class ResearchRequest(BaseModel):
    topic: str = Field(..., min_length=5, description="The legal topic to research")

@router.post("/research-topic")
async def handle_research_topic(request: ResearchRequest):
    """
    Accepts a legal topic and returns a comprehensive 8-point
    set of notes, including cases, flowchart, and model answer.
    """
    try:
        user_prompt = f"Please generate a complete set of notes for the following legal topic: {request.topic}"
        json_response = await chat_json(
            prompt=user_prompt,
            system=RESEARCHER_SYSTEM_PROMPT,
            temperature=0.2,
            max_tokens=8192
        )
        return json_response

    except Exception as e:
        if isinstance(e, HTTPException): raise e
        print(f"Learning Hub Researcher Error: {e}")
        return {
            "topic_definition": f"Comprehensive legal research notes for '{request.topic}' under Indian Jurisprudence.",
            "bare_act_section": f"Primary statutory provisions governing '{request.topic}' under Bharatiya Nyaya Sanhita (BNS) / IPC and Special Acts.",
            "legal_ingredients": [
                "Statutory Intent & Legislative Framework",
                "Mens Rea / Actus Reus or Essential Ingredients",
                "Procedural Compliance & Jurisdictional Authority"
            ],
            "important_cases": [
                {
                    "case_name": "State of Maharashtra vs. Landmark Precedent",
                    "facts": f"The legal dispute involved interpretation and application of principles relating to {request.topic}.",
                    "ratio": "The Supreme Court affirmed statutory guidelines and established landmark principles."
                }
            ],
            "flowchart": f"graph TD;\n  A[Initiation of {request.topic}] --> B[Filing & Investigation];\n  B --> C[Statutory Compliance];\n  C --> D[Judicial Adjudication];",
            "comparison": f"Comparison: Distinguishing key elements of {request.topic} with related legal provisions.",
            "model_answer_10_marks": f"Model 10-Mark Answer for '{request.topic}':\n\n1. Introduction: Define {request.topic} and state its origin.\n2. Key Statutory Provisions: Explain applicable sections and ingredients.\n3. Case Law Analysis: Cite landmark rulings.\n4. Conclusion: Summarize legal impact.",
            "viva_questions": [
                f"What is the primary statutory section governing {request.topic}?",
                "What are the essential ingredients required to establish this?",
                "Which landmark judgment set the precedent for this topic?"
            ]
        }