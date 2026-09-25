from fastapi import APIRouter, HTTPException, UploadFile, File, Depends
from typing import Optional
import pypdf
import docx
import io
import json
from datetime import date
from docxtpl import DocxTemplate
import docx2pdf
import os
import re
import time

from app.services.ollama_client import chat_json
from app.core.security import get_current_user


async def get_optional_user(current_user: Optional[dict] = Depends(get_current_user)) -> Optional[dict]:
    """Optional authentication - returns None if not authenticated instead of raising 401."""
    return current_user

# --- JSON SCHEMA PROMPT FOR DEEP LEGAL CASE SUMMARIZATION ---
SUMMARIZER_SYSTEM_PROMPT = """
You are a senior Indian Legal Research Analyst and Legal Expert. Deeply analyze the provided Indian Court Judgment or Legal Document and generate an exhaustive, highly accurate, and comprehensive legal summary.

**EXACT INSTRUCTIONS FOR EXTRACTION & ANALYSIS:**
1. **Case Title & Meta Info:**
   - **Case Name:** Extract full title (e.g., "State of Maharashtra vs. Ramesh Sharma & Ors.").
   - **Case Number:** Identify Appeal/Petition No. (e.g., "Civil Appeal No. 4521 of 2021", "SLP (Crl.) No. 8910/2022").
   - **Court Name:** Name of Court (e.g., "Supreme Court of India", "High Court of Judicature at Bombay").
   - **Jurisdiction:** Specific division (e.g., "Civil Appellate Jurisdiction", "Criminal Original Jurisdiction").
   - **Citations:** All official law report citations mentioned (e.g., "2023 INSC 412", "(2022) 4 SCC 100").

2. **Parties & Legal Counsel:**
   - **Petitioner / Appellant:** Name of Petitioner(s) or Appellant(s).
   - **Respondent:** Name of Respondent(s).
   - **Advocates Petitioner:** Names of Senior Advocates and Counsel representing Petitioner/Appellant.
   - **Advocates Respondent:** Names of Senior Advocates and Counsel representing Respondent.

3. **Dates:**
   - **Date of Judgment:** Official date when judgment was delivered.
   - **Date of Filing:** Date of institution or filing of petition (or "" if not mentioned).

4. **Sections Invoked & Acts:**
   - Provide a comprehensive list of all Acts, Sections, Articles, and Rules cited (e.g., "Sections 302, 304B, 498A IPC; Section 113B Evidence Act; Article 21 Constitution of India").

5. **Legal Issues & Questions of Law:**
   - List every key legal issue or question of law framed or decided by the court as clear, detailed statements.

6. **Final Judgment, Ratio Decidendi & Relief Granted:**
   - Provide a detailed, comprehensive multi-paragraph summary covering:
     a) Factual Background of the dispute.
     b) Core arguments of both Petitioner and Respondent.
     c) Ratio Decidendi & Legal Reasoning of the Judges.
     d) Final Order, verdict, sentence, compensation, or directions issued by the Court.

**CRITICAL:** Return ONLY a valid JSON object matching this structure:

{
  "case_title_info": {
    "case_name": "string",
    "case_number": "string",
    "court_name": "string",
    "jurisdiction": "string",
    "citations": "string"
  },
  "parties_involved": {
    "petitioner": "string",
    "respondent": "string",
    "advocates_petitioner": "string",
    "advocates_respondent": "string"
  },
  "dates": {
    "date_of_judgment": "string",
    "date_of_filing": "string"
  },
  "sections_invoked": "string",
  "legal_issues": ["string"],
  "final_judgment": "string"
}
"""

router = APIRouter(
    prefix="/api/v1/summarizer",
    tags=["CaseSummarizer"]
)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
APP_DIR = os.path.dirname(BASE_DIR)
TEMPLATE_DIR = os.path.join(APP_DIR, "templates")
OUTPUT_DIR = os.path.join(APP_DIR, "static/outputs")
os.makedirs(OUTPUT_DIR, exist_ok=True)

def get_default_schema_dict():
    return {
        "case_title_info": {
            "case_name": "", "case_number": "", "court_name": "", "jurisdiction": "", "citations": ""
        },
        "parties_involved": {
            "petitioner": "", "respondent": "", "advocates_petitioner": "", "advocates_respondent": ""
        },
        "dates": {
            "date_of_judgment": "", "date_of_filing": ""
        },
        "sections_invoked": "",
        "legal_issues": [],
        "final_judgment": ""
    }

# --- Helpers (Unchanged) ---
def get_template(template_name):
    template_path = os.path.join(TEMPLATE_DIR, template_name)
    if not os.path.exists(template_path): return None
    return DocxTemplate(template_path)

def sanitize_filename(text):
    text = re.sub(r'[^\w\s-]', '', text).strip()
    text = re.sub(r'[-\s]+', '-', text)
    return text

def generate_and_save_files(doc: DocxTemplate, context: dict, base_filename: str):
    timestamp = int(time.time())
    unique_filename = f"{base_filename}_{timestamp}"
    output_docx_name = f"{unique_filename}.docx"
    output_pdf_name = f"{unique_filename}.pdf"
    output_docx_path = os.path.join(OUTPUT_DIR, output_docx_name)
    output_pdf_path = os.path.join(OUTPUT_DIR, output_pdf_name)

    doc.render(context)
    doc.save(output_docx_path)
    try:
        docx2pdf.convert(output_docx_path, output_pdf_path)
        pdf_url = f"/static/outputs/{output_pdf_name}"
    except Exception as pdf_error:
        print(f"Warning: PDF conversion failed: {pdf_error}")
        pdf_url = None

    return {
        "docx_url": f"/static/outputs/{output_docx_name}",
        "pdf_url": pdf_url
    }

def extract_text_from_pdf(file_stream: io.BytesIO) -> str:
    try:
        reader = pypdf.PdfReader(file_stream)
        text = ""
        for page in reader.pages: text += page.extract_text() or ""
        return text.replace('\x00', '')
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"PDF Error: {e}")

def extract_text_from_docx(file_stream: io.BytesIO) -> str:
    try:
        document = docx.Document(file_stream)
        text = ""
        for para in document.paragraphs: text += para.text + "\n"
        return text.replace('\x00', '')
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"DOCX Error: {e}")

@router.post("/upload-and-summarize")
async def handle_summarize_upload(
    file: UploadFile = File(...),
    current_user: Optional[dict] = Depends(get_optional_user)
):
    print("[summarizer] request start")
    MAX_FILE_SIZE = 15 * 1024 * 1024  # 15 MB limit
    try:
        contents = await file.read()
        if not contents:
            raise HTTPException(status_code=400, detail="Empty file.")
        
        if len(contents) > MAX_FILE_SIZE:
            raise HTTPException(status_code=400, detail="File size exceeds maximum 15MB limit.")
        
        file_stream = io.BytesIO(contents)
        if file.content_type == "application/pdf":
            print("[summarizer] reading pdf")
            document_text = extract_text_from_pdf(file_stream)
        elif "wordprocessingml" in file.content_type:
            print("[summarizer] reading docx")
            document_text = extract_text_from_docx(file_stream)
        elif "text" in file.content_type:
            print("[summarizer] reading text")
            document_text = contents.decode("utf-8").replace('\x00', '')
        else:
            raise HTTPException(status_code=400, detail="Invalid file type.")

        if not document_text.strip():
            raise HTTPException(status_code=400, detail="Extracted text is empty.")
    except Exception as e:
        print(f"File read error: {e}")
        raise HTTPException(status_code=500, detail=f"File Error: {str(e)}")

    MAX_CHARS = 400000 
    if len(document_text) > MAX_CHARS:
        print(f"[summarizer] truncating text from {len(document_text)} chars")
        document_text = document_text[:MAX_CHARS]

    try:
        print("[summarizer] calling ollama")
        json_response = await chat_json(
            prompt=document_text,
            system=SUMMARIZER_SYSTEM_PROMPT,
            temperature=0.2,
            max_tokens=8192
        )
        print("[summarizer] ollama response ok")
    except Exception as e:
        print(f"AI Error: {e}")
        raise HTTPException(status_code=500, detail=f"AI Error: {str(e)}")

    try:
        doc = get_template("summary_template.docx")
        if doc is None: raise HTTPException(status_code=500, detail="Template not found.")
        
        # Merge defaults
        full_ai_response = get_default_schema_dict()
        def deep_merge(source, destination):
            for key, value in source.items():
                if isinstance(value, dict):
                    node = destination.setdefault(key, {})
                    deep_merge(value, node)
                else:
                    destination[key] = value
            return destination
        full_ai_response = deep_merge(json_response, full_ai_response)

        # --- MAP TO TEMPLATE VARIABLES ---
        # This maps the AI JSON to your Docx template tags
        template_context = {
            'case_name': full_ai_response['case_title_info'].get('case_name', ''),
            'case_number': full_ai_response['case_title_info'].get('case_number', ''),
            'court_name': full_ai_response['case_title_info'].get('court_name', ''),
            'jurisdiction': full_ai_response['case_title_info'].get('jurisdiction', ''),
            'citations': full_ai_response['case_title_info'].get('citations', ''),
            
            'petitioner': full_ai_response['parties_involved'].get('petitioner', ''),
            'respondent': full_ai_response['parties_involved'].get('respondent', ''),
            'adv_petitioner': full_ai_response['parties_involved'].get('advocates_petitioner', ''),
            'adv_respondent': full_ai_response['parties_involved'].get('advocates_respondent', ''),
            
            'judgment_date': full_ai_response['dates'].get('date_of_judgment', ''),
            'filing_date': full_ai_response['dates'].get('date_of_filing', ''),
            
            'sections': full_ai_response.get('sections_invoked', ''),
            'issues': full_ai_response.get('legal_issues', []),
            'final_judgment': full_ai_response.get('final_judgment', ''),
        }
        
        base_filename = f"summary_{sanitize_filename(template_context['case_name'])}"
        download_links = generate_and_save_files(doc, template_context, base_filename)

        return {
            "summary_data": full_ai_response, 
            "download_links": download_links
        }

    except Exception as e:
        print(f"Gen Error: {e}")
        raise HTTPException(status_code=500, detail=f"Gen Error: {str(e)}")
