
# ⚖️ Vani-Kanoon — AI Legal Assistant

[![FastAPI](https://img.shields.io/badge/FastAPI-0.100+-009688.svg?style=flat&logo=fastapi)](https://fastapi.tiangolo.com/)
[![React](https://img.shields.io/badge/React-18+-61DAFB.svg?style=flat&logo=react)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-4+-646CFF.svg?style=flat&logo=vite)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3.3+-38B2AC.svg?style=flat&logo=tailwind-css)](https://tailwindcss.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1.svg?style=flat&logo=mysql)](https://www.mysql.com/)
[![Python](https://img.shields.io/badge/Python-3.11+-3776AB.svg?style=flat&logo=python)](https://www.python.org/)

**Vani-Kanoon** is a comprehensive, full-stack AI-powered legal assistant platform designed to democratize legal access. It offers multilingual voice interactions, legal document drafting, FIR analysis, case summarization, automated notice generation, interactive legal education, and secure user authentication.

---

## ✨ Features

- 🎙️ **Vani-Kanoon Multilingual Voice Assistant**: Voice-enabled legal assistance supporting multiple languages (Hindi, English, regional Indian languages) with integrated Text-to-Speech (gTTS).
- 💬 **AI Legal Chatbot**: Conversational Q&A with historical chat logging backed by MySQL.
- 📄 **Legal Document Generator**: Automated generation of Rent Agreements, Sale Deeds, Lease Deeds, Employment Contracts, and NDAs.
- 🔍 **FIR & Document Analyzer**: OCR-enabled document processing (PyTesseract) and AI analysis of FIRs, court orders, and legal notices.
- 📑 **Case Summarizer**: Concise AI summarization of legal precedents and court judgments.
- ✉️ **Legal Notice Generator**: Customizable legal notice draft generator.
- 📚 **Learning Hub & FAQ Builder**: Educational modules on citizen rights, laws, and dynamic FAQ generation.
- 🔐 **Authentication & User Profiles**: User signup, login, profile management, and password reset functionality.
- 📱 **Cross-Platform / Mobile Ready**: Configured with Capacitor for Android native application builds.

---

## 🏗️ Project Architecture

```
ai-legal-assistant-main/
├── backend/
│   ├── app/
│   │   ├── api/             # FastAPI Routers (Auth, Chatbot, Documents, Vani Voice, etc.)
│   │   ├── core/            # Database config, security, settings
│   │   ├── models/          # Data models and Pydantic schemas
│   │   ├── services/        # Gemini LLM services, OCR, PDF generator
│   │   └── static/          # Static files & generated PDF/Word output assets
│   ├── create_tables.py     # MySQL database initialization script
│   ├── .env                 # API Keys & DB Credentials
│   └── requirements.txt     # Python dependencies
│
├── frontend/
│   ├── src/
│   │   ├── components/      # UI Layout components, Navigation Bar, Footer
│   │   ├── pages/           # Feature pages (VaniKanoon, Chatbot, DocumentGenerator, etc.)
│   │   └── services/        # API communication & fetch handlers
│   ├── capacitor.config.json # Mobile application config
│   ├── package.json         # Node dependencies
│   └── vite.config.js       # Vite build & PWA setup
│
├── database/                # Database seeds and SQL schema scripts
└── scripts/                 # Utility scripts for embeddings & FAQ updates
```

---

## ⚙️ Tech Stack

- **Frontend**: React 18, Vite, Tailwind CSS, Lucide React, React Router v7, Capacitor.
- **Backend**: FastAPI, Uvicorn, Python 3.11, Pydantic, gTTS, PyTesseract, python-docx.
- **AI & ML**: Google Gemini API (GenAI), Google Generative AI Python SDK.
- **Database**: MySQL (with `mysql-connector-python`).

---

## 🚀 Quick Start Guide

### Prerequisites

- **Python**: `3.11+`
- **Node.js**: `18.0+`
- **MySQL Server**: Running on `localhost:3306`

---

### 1. Database Setup

1. Start your local **MySQL Server**.
2. Configure credentials in `backend/.env` (or update existing settings):
   ```env
   DB_USER=root
   DB_PASSWORD=your_password
   DB_HOST=localhost
   DB_NAME=ai_legal_db
   ```
3. Initialize database tables:
   ```bash
   cd backend
   .venv\Scripts\python.exe create_tables.py
   ```

---

### 2. Backend Setup (FastAPI)

1. Navigate to the `backend` directory:
   ```bash
   cd backend
   ```
2. Activate the virtual environment (or create one):
   ```bash
   # On Windows
   .venv\Scripts\activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Verify environment variables in `backend/.env`:
   ```env
   GOOGLE_API_KEY=your_gemini_api_key
   GENAI_API_KEY=your_gemini_api_key
   ```
5. Launch the backend server:
   ```bash
   python -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
   ```
   - API Server: [http://127.0.0.1:8000](http://127.0.0.1:8000)
   - Interactive OpenAPI Docs: [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)

---

### 3. Frontend Setup (React + Vite)

1. Open a new terminal and navigate to the `frontend` directory:
   ```bash
   cd frontend
   ```
2. Install npm packages:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
   - Access the web interface at: [http://localhost:5173](http://localhost:5173)

---

## 📱 Building Mobile App (Android)

To export the frontend as an Android application using Capacitor:

```bash
cd frontend
npm run mobile:build
npm run mobile:open
```

---

## 🛠️ API Reference Summary

- `POST /api/auth/register` — User Registration
- `POST /api/auth/login` — User Authentication
- `POST /api/vani/chat` — Multilingual Voice & Text Legal Query
- `POST /api/documents/generate` — Generate Legal Agreements (PDF/DOCX)
- `POST /api/analyzer/analyze-fir` — Analyze FIR / OCR Legal Document
- `POST /api/summarizer/summarize-case` — Case Law Summarizer
- `POST /api/notice/generate` — Legal Notice Generator

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
