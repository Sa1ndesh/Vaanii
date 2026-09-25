# Vani-Kanoon - Setup and Run Guide

## Prerequisites

| Requirement | Version | Check Command |
|-------------|---------|---------------|
| Python | 3.11+ | `python --version` |
| Node.js | 18.0+ | `node --version` |
| npm | 9.0+ | `npm --version` |
| MySQL Server | 8.0+ | `mysql --version` |

---

## 1. Database Setup

### 1.1 Start MySQL Server
Ensure MySQL is running on `localhost:3306`.

### 1.2 Create the Database
```sql
mysql -u root -p
CREATE DATABASE ai_legal_db;
EXIT;
```

### 1.3 Configure Environment Variables
Create or edit `backend/.env`:
```env
DB_USER=root
DB_PASSWORD=your_mysql_password
DB_HOST=localhost
DB_NAME=ai_legal_db

GOOGLE_API_KEY=your_gemini_api_key
GENAI_API_KEY=your_gemini_api_key
```

### 1.4 Initialize Tables
```bash
cd backend
python create_tables.py
```

Expected output:
```
Successfully connected to database 'ai_legal_db'
Attempting to create table: users... Created!
Attempting to create table: chat_history... Created!
```

---

## 2. Backend Setup (FastAPI)

### 2.1 Create Virtual Environment
```bash
cd backend
python -m venv .venv
```

### 2.2 Activate Virtual Environment

**Windows (CMD):**
```cmd
.venv\Scripts\activate
```

**Windows (PowerShell):**
```powershell
.venv\Scripts\Activate.ps1
```

**Linux/macOS:**
```bash
source .venv/bin/activate
```

### 2.3 Install Dependencies
```bash
pip install -r requirements.txt
```

### 2.4 Run the Backend Server
```bash
python -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
```

**Verify:** Open http://127.0.0.1:8000/docs to see the Swagger API documentation.

---

## 3. Frontend Setup (React + Vite)

### 3.1 Install Dependencies
```bash
cd frontend
npm install
```

### 3.2 Configure API Endpoint (if needed)
Edit `frontend/src/config.js` to point to your backend:
```javascript
export const API_BASE_URL = 'http://127.0.0.1:8000';
```

### 3.3 Run the Frontend Dev Server
```bash
npm run dev
```

**Verify:** Open http://localhost:5173 in your browser.

---

## 4. Running Both Servers

Open two terminal windows:

**Terminal 1 - Backend:**
```bash
cd backend
.venv\Scripts\activate
python -m uvicorn app.main:app --reload --host 127.0.0.1 --port 8000
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm run dev
```

---

## 5. Testing the Login Flow

1. Open http://localhost:5173
2. Navigate to **Sign Up** page
3. Register a new user with:
   - Name
   - Email
   - Password
   - Phone (optional)
4. After registration, navigate to **Login**
5. Enter credentials and submit
6. On success, you should be redirected to the dashboard

**API Endpoints for Auth:**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/forgot-password` - Password reset request

---

## 6. Running the ML Pipeline

The ML scripts are located in the `scripts/` directory.

### Generate Embeddings
```bash
cd scripts
python generate_embeddings.py
```

### Update FAQs
```bash
python update_faqs.py
```

### Train Summarizer
```bash
python train_summarizer.py
```

### Retrain Classifier
```bash
python retrain_classifier.py
```

### Full Training Pipeline
```bash
cd scripts/training
python run_pipeline.py
```

### Data Collection Scripts
```bash
cd scripts/data_collection
python scrape_india_code.py
python scrape_indian_kanoon.py
python create_training_data.py
```

---

## 7. Common Errors and Fixes

### Database Connection Errors

**Error:** `Access denied for user 'root'@'localhost'`
- **Fix:** Check `DB_USER` and `DB_PASSWORD` in `backend/.env`

**Error:** `Unknown database 'ai_legal_db'`
- **Fix:** Create the database first:
  ```sql
  CREATE DATABASE ai_legal_db;
  ```

**Error:** `Can't connect to MySQL server on 'localhost'`
- **Fix:** Ensure MySQL service is running:
  ```bash
  # Windows
  net start mysql
  
  # Linux
  sudo systemctl start mysql
  ```

### Backend Errors

**Error:** `ModuleNotFoundError: No module named 'xyz'`
- **Fix:** Ensure virtual environment is activated and dependencies installed:
  ```bash
  .venv\Scripts\activate
  pip install -r requirements.txt
  ```

**Error:** `GOOGLE_API_KEY not found`
- **Fix:** Add your Gemini API key to `backend/.env`

**Error:** Port 8000 already in use
- **Fix:** Kill the process or use a different port:
  ```bash
  python -m uvicorn app.main:app --reload --port 8001
  ```

### Frontend Errors

**Error:** `ENOENT: no such file or directory, node_modules`
- **Fix:** Run `npm install` in the frontend directory

**Error:** `fetch failed` / CORS errors
- **Fix:** Ensure backend is running on http://127.0.0.1:8000

**Error:** Port 5173 already in use
- **Fix:** Vite will auto-select next port, or specify:
  ```bash
  npm run dev -- --port 3000
  ```

### General Tips

1. Always activate the virtual environment before running backend commands
2. Ensure both backend and frontend are running simultaneously
3. Check the browser console (F12) for frontend errors
4. Check the terminal for backend error logs
5. Verify `.env` file has no extra spaces around `=` signs

---

## 8. API Quick Reference

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/register` | POST | User registration |
| `/api/auth/login` | POST | User login |
| `/api/vani/chat` | POST | Voice/text legal query |
| `/api/documents/generate` | POST | Generate legal documents |
| `/api/analyzer/analyze-fir` | POST | Analyze FIR documents |
| `/api/summarizer/summarize-case` | POST | Summarize case law |
| `/api/notice/generate` | POST | Generate legal notices |

Full API documentation: http://127.0.0.1:8000/docs

---

## 9. Building for Production

### Backend
```bash
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### Frontend
```bash
cd frontend
npm run build
npm run preview
```

### Mobile (Android)
```bash
cd frontend
npm run mobile:build
npm run mobile:open
```
