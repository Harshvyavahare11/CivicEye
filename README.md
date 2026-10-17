# CivicEye — AI-Powered Civic Issue Reporting Platform (V1)

CivicEye empowers citizens to photograph, analyze, and report visible civic infrastructure problems (such as potholes, open drains, garbage dumps, broken streetlights, and waterlogging) using computer vision and hosted **Google Gemma 4 AI**.

The application automatically categorizes issues, assesses preliminary severity, determines jurisdiction, recommends next steps, and drafts a clear, respectful grievance letter ready for citizen review and submission.

> **Important V1 Scope Disclaimer**: CivicEye V1 prepares, organizes, and manages civic complaints locally. It does not automatically submit complaints to government or municipal portals unless an authorized municipal API gateway is integrated.

---

## 1. System Architecture

```text
React Frontend (Vite)
    Port: 5173
    URL: http://localhost:5173
        │
        │ REST API (JSON & Multipart FormData)
        ▼
Spring Boot Backend
    Port: 8080
    URL: http://localhost:8080
        │
        │ Forwarding /analyze & checking health
        ▼
Python FastAPI AI Engine
    Port: 8000
    URL: http://localhost:8000 (Swagger: /docs)
        │
        │ Hosted Gemma 4 API (https://generativelanguage.googleapis.com)
        ▼
Google Gemma 4 (gemma-4-26b-a4b-it)
```

---

## 2. Prerequisites

Verify installed tools on Windows:

- **Node.js**: v18+ (tested on Node v25.1.0) and **npm**
- **Java**: Java 17 or Java 21 LTS (tested on Java 21.0.2)
- **Maven**: 3.8+ (tested on Maven 3.9.12)
- **Python**: 3.11+ (tested on Python 3.14.0) and **pip**

---

## 3. Environment Setup & Configuration

### A. Root Configuration
Copy the top-level example:
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye
Copy-Item .env.example .env
```

### B. AI Engine Configuration
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\AI-engine
Copy-Item .env.example .env
```
Open `AI-engine/.env` and configure:
```env
AI_HOST=127.0.0.1
AI_PORT=8000
GEMMA_API_KEY=your_actual_google_gemma_api_key_here
GEMMA_MODEL=gemma-4-26b-a4b-it
DEMO_MODE_DEFAULT=false
```
*Note: If you do not have an active Google Gemma 4 API key, leave `GEMMA_API_KEY` blank or enable Demo Mode in the web interface to test with deterministic simulated responses.*

### C. Backend Configuration
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\backend
Copy-Item .env.example .env
```

### D. Frontend Configuration
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\Frontend
Copy-Item .env.example .env
```

---

## 4. Running the Application (3 Independent Services)

Open three separate Windows PowerShell windows to launch the full stack:

### Terminal 1: Python AI Engine (Port 8000)
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\AI-engine
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```
- Health Check: `GET http://127.0.0.1:8000/health`
- Interactive Swagger UI: `http://127.0.0.1:8000/docs`

### Terminal 2: Spring Boot Backend (Port 8080)
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\backend
mvn spring-boot:run
```
- Health Check: `GET http://localhost:8080/api/health`
- AI Status: `GET http://localhost:8080/api/ai/health`
- Report List: `GET http://localhost:8080/api/reports`

### Terminal 3: React Frontend (Port 5173)
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\Frontend
npm install
npm run dev
```
Open your browser and navigate to: **`http://localhost:5173`**

---

## 5. End-to-End Primary User Journey

1. **Dashboard & Status Check**:
   - Open `http://localhost:5173`.
   - Observe live status badges: "Backend Connected" (green) and "AI Engine Live / Demo Available".
   - Notice real statistics calculated from the repository.
2. **Report an Issue**:
   - Click **"Report an Issue"** in the navigation bar.
   - Drag and drop or select a civic issue photo (e.g., JPEG, PNG, or WebP).
   - Enter an optional description (e.g. *"Large road depression causing sudden vehicular braking"*).
   - Click **"Use Current GPS"** or type a manual landmark (e.g. *"MG Road near Metro Pillar 42"*).
   - Toggle **"Demo Mode"** if testing without live Google Gemma 4 API keys.
   - Click **"Analyze Issue with AI"**.
3. **Review & Edit AI Analysis**:
   - View detected category, preliminary severity with rationale, responsible department, and physical verification factors.
   - Review and customize the editable complaint text draft.
   - Click **"Save Report to My Reports"**.
4. **Manage Reports in My Reports**:
   - Search reports by keyword or filter by status (`Draft`, `Ready for Submission`, `Submitted`, `Resolved`).
   - Click **"View Details"** to inspect full evidence.
   - Click **"Edit Draft"** to update complaint text or lifecycle status.
   - Delete test reports with confirmation.

---

## 6. Demo Mode vs. Live AI Mode

CivicEye includes an explicit **Demo Mode** ensuring the prototype is demonstrable under all conditions:
- **Live AI Mode**: Calls the hosted Google Gemma 4 API via `https://generativelanguage.googleapis.com` using your `GEMMA_API_KEY`.
- **Demo Mode**: Produces deterministic simulated municipal assessments based on image context without consuming API quota.
- When Demo Mode is used, the UI prominently displays a blue **"Demo Mode — Simulated AI Response"** banner so users are never misled.

---

## 7. Running Automated Tests

### Backend Tests (Spring Boot + MockMvc)
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\backend
mvn test "-Djava.net.preferIPv4Stack=true"
```
Verifies:
- `GET /api/health` returns `200 OK` and `"UP"`.
- `GET /api/ai/health` handles service connectivity.
- `POST /api/reports/analyze` rejects empty uploads (`400`) and unsupported types (`415`).
- `GET /api/reports` returns seeded reports.
- `POST /api/reports` creates reports (`201`).

### AI Engine Tests (Pytest)
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\AI-engine
pytest
```
Verifies:
- `GET /health` endpoint response and configuration flags.
- Rejection of empty images (`400`) and non-image types (`415`).
- Deterministic simulated responses in Demo Mode.

---

## 8. Troubleshooting & Tips

- **Connection Reset / Maven download errors**:
  If your ISP or router has IPv6 routing issues, add `-Djava.net.preferIPv4Stack=true` to your Maven command:
  ```powershell
  mvn clean test "-Djava.net.preferIPv4Stack=true"
  ```
- **Port Conflicts**:
  If port `8080`, `8000`, or `5173` is occupied, change `PORT=8081` in `backend/.env` or `AI_PORT=8001` in `AI-engine/.env`.
- **Gemma Model Not Found (404)**:
  Verify your Google AI Studio account has access to `gemma-4-26b-a4b-it`. You can switch models in `AI-engine/.env` by editing `GEMMA_MODEL`.

---

## 9. Known Limitations & Future Enhancements

- **V1 Limitations**:
  - In-memory thread-safe storage (reports survive process restart but do not persist across JVM shutdown).
  - No user authentication or role-based access control.
  - V1 prepares grievance drafts; it does not dispatch directly to government APIs.
- **Planned V2 Roadmap**:
  - Persistent PostgreSQL database via Spring Data JPA (see [`DATABASE_SCHEMA.md`](file:///c:/Users/aliba/Desktop/AIT/CivicEye/docs/DATABASE_SCHEMA.md)).
  - Interactive Leaflet / OpenStreetMap GPS pin-drop coordinate selector.
  - Citizen SMS/Email status notification gateway.
  - Municipal Admin Portal for ward inspectors to update resolution status with after-repair photographs.
