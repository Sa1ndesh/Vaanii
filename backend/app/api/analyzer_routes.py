from typing import Optional
from fastapi import APIRouter, HTTPException, UploadFile, File, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import pypdf
import docx
import io
import json
import pytesseract
from PIL import Image

from app.services.ollama_client import chat_json
from app.services.image_enhancer import enhance_image_for_ocr
from app.core.security import decode_access_token
import os

# Optional auth bearer - does not auto-error when token is missing
optional_bearer = HTTPBearer(auto_error=False)


def get_current_user_optional(
    auth: HTTPAuthorizationCredentials | None = Depends(optional_bearer)
) -> Optional[dict]:
    """Optional auth dependency - returns user payload if valid token provided, None otherwise."""
    if not auth or not auth.credentials:
        return None
    payload = decode_access_token(auth.credentials)
    if not payload or "sub" not in payload:
        return None
    return payload

# --- Tesseract Configuration for Windows ---
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'
# Get absolute path to tessdata folder in the current directory
BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
TESSDATA_PATH = os.path.join(BASE_DIR, "tessdata")

# Tesseract on Windows is very sensitive to paths. 
# We set TESSDATA_PREFIX to the folder containing the traineddata files.
os.environ['TESSDATA_PREFIX'] = TESSDATA_PATH.replace('\\', '/')

# --- System Prompt for Extracting FIR Data via Ollama ---
ANALYZER_SYSTEM_PROMPT = """
You are an AI assistant that extracts key details from an Indian First Information Report (FIR) or related police document.
The user will provide the full text.
Your task is to find the 11 key pieces of information.
You MUST respond *only* with a single, valid JSON object that adheres to the following structure.
Do not add any conversational text.
If you cannot find information for a field, return the string "data is not present in the file".
For the 'witnesses' field, return an array of strings. If no witnesses are mentioned, return an empty array [].

{
  "fir_number": "string",
  "police_station": "string",
  "date_of_filing": "string",
  "complainant": "string",
  "date_and_time_of_incident": "string",
  "place_of_incident": "string",
  "accused_name": "string",
  "witnesses": ["string"],
  "offence": "string",
  "offences_mentioned": "string",
  "investigating_officer": "string"
}
"""

# --- API Configuration ---
router = APIRouter(
    prefix="/api/v1/analyzer",
    tags=["FIRAnalyzer"]
)

# --- Helper functions for file reading ---
def extract_text_from_pdf(file_stream: io.BytesIO) -> str:
    try:
        reader = pypdf.PdfReader(file_stream)
        text = ""
        for page in reader.pages:
            text += page.extract_text() or ""
        return text
    except Exception as e:
        print(f"Error reading PDF: {e}")
        raise HTTPException(status_code=400, detail=f"Could not read PDF. File may be corrupt or encrypted: {e}")

def extract_text_from_docx(file_stream: io.BytesIO) -> str:
    try:
        document = docx.Document(file_stream)
        text = ""
        for para in document.paragraphs:
            text += para.text + "\n"
        return text
    except Exception as e:
        print(f"Error reading DOCX: {e}")
        raise HTTPException(status_code=400, detail=f"Could not read DOCX file: {e}")

def extract_text_from_image(contents: bytes) -> str:
    try:
        # 1. Enhance the image (CLAHE, Denoising, Sharpening)
        enhanced_image = enhance_image_for_ocr(contents)
        
        # 2. Run OCR on the enhanced image with multi-language support
        # Config is now handled via TESSDATA_PREFIX env var
        custom_config = r'--psm 3'
        print(f"Running OCR with languages: eng+kan+hin")
        
        text = pytesseract.image_to_string(enhanced_image, lang='eng+kan+hin', config=custom_config)
        
        # Debug print to see if we got anything
        clean_text = text.strip()
        if not clean_text:
            print("OCR returned empty text.")
        else:
            print(f"OCR extracted {len(clean_text)} characters. Preview: {clean_text[:100]}...")
            
        return clean_text
    except Exception as e:
        print(f"Error reading image via OCR: {e}")
        # If it fails, try without custom tessdata as a last resort
        try:
            print("Retrying OCR with default system settings...")
            # Unset prefix for fallback to system default
            if 'TESSDATA_PREFIX' in os.environ:
                del os.environ['TESSDATA_PREFIX']
            return pytesseract.image_to_string(Image.open(io.BytesIO(contents)))
        except Exception as retry_e:
            print(f"Fallback OCR also failed: {retry_e}")
            raise HTTPException(status_code=400, detail=f"Could not extract text from image: {e}")

@router.post("/analyze-fir")
async def handle_fir_analysis(
    file: UploadFile = File(...),
    current_user: Optional[dict] = Depends(get_current_user_optional)
):
    """
    Accepts a PDF, DOCX, Image, or TXT file, validates size (<15MB), extracts text, and returns a
    structured JSON of key FIR details.
    """
    MAX_FILE_SIZE = 15 * 1024 * 1024  # 15 MB limit
    try:
        contents = await file.read()
        if not contents:
            raise HTTPException(status_code=400, detail="The uploaded file is empty.")

        if len(contents) > MAX_FILE_SIZE:
            raise HTTPException(status_code=400, detail="File size exceeds maximum 15MB limit.")
        
        file_stream = io.BytesIO(contents)
        
        if file.content_type == "application/pdf":
            document_text = extract_text_from_pdf(file_stream)
        elif "wordprocessingml" in file.content_type:
            document_text = extract_text_from_docx(file_stream)
        elif "image/" in file.content_type:
            document_text = extract_text_from_image(contents)
        elif "text" in file.content_type:
            document_text = contents.decode("utf-8")
        else:
            raise HTTPException(status_code=400, detail="Invalid file type. Please upload a PDF, DOCX, Image, or TXT file.")

        if not document_text.strip():
            raise HTTPException(status_code=400, detail="Uploaded file is empty or text could not be extracted.")
            
    except Exception as e:
        if isinstance(e, HTTPException):
            raise e
        print(f"File read error: {e}")
        raise HTTPException(status_code=500, detail=f"Error reading file: {str(e)}")

    # 2. Send text to Ollama for structured JSON
    try:
        json_response = await chat_json(
            prompt=document_text,
            system=ANALYZER_SYSTEM_PROMPT,
            temperature=0.0,
            max_tokens=2048
        )
        return json_response

    except Exception as e:
        if isinstance(e, HTTPException):
            raise e
        print(f"An error occurred with Ollama: {e}")
        raise HTTPException(status_code=500, detail=f"Error processing document with AI: {str(e)}")