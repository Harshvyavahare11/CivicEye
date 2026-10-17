# CivicEye Team Task Breakdown & Hackathon Collaboration Matrix

This task matrix assigns domain responsibilities across 5 team members. All team members adhere strictly to the contract defined in [`API_CONTRACT.md`](file:///c:/Users/aliba/Desktop/AIT/CivicEye/docs/API_CONTRACT.md).

---

## Team Assignment Matrix

| Person | Primary Role | Domain Focus | Key Responsibilities |
| :--- | :--- | :--- | :--- |
| **Person 1** | Frontend Architect | Layout, Dashboard, Navigation & Design System | App navigation, UI theme/styling, responsive layouts, shared components (`Navbar`, `Footer`, `StatusBadge`, `Alert`), Dashboard stats and health indicators. |
| **Person 2** | Frontend Engineer | Reporting Workflow & API Integration | Issue reporting form, file upload & preview, location detection, live Gemma analysis display, complaint draft editor, report saving, and My Reports list/details views. |
| **Person 3** | Backend Engineer | REST Controllers, DTOs & Validation | Spring Boot application setup, `HealthController`, `AiHealthController`, `ReportController`, file upload validation (JPEG/PNG/WebP, size limits), CORS configuration, and centralized exception handling. |
| **Person 4** | Backend Architect | Persistence, Report Management & Tests | Domain model (`Report.java`), `ReportRepository` and `InMemoryReportRepository`, report filtering and stats logic, automated Spring Boot controller tests, and database schema documentation. |
| **Person 5** | AI / ML Engineer | FastAPI Service & Hosted Gemma 4 Integration | FastAPI service, hosted Google Gemma 4 API client (`gemma-4-26b-a4b-it`), prompt engineering with strict municipal constraints, Pydantic schema validation, deterministic Demo Mode simulation, and AI unit tests. |

---

## Detailed Task Breakdown

### Person 1 — Frontend (Layout, Design System & Dashboard)
- [x] Configure Vite, React Router, and unified CSS design system in `Frontend/src/index.css`.
- [x] Create reusable UI building blocks in `components/common/` (`Navbar`, `Footer`, `StatusBadge`, `Alert`).
- [x] Implement base app shell in `components/layout/Layout.jsx`.
- [x] Build interactive Dashboard on `pages/Home.jsx` showing real statistics calculated from current reports.
- [x] Live polling of backend and AI engine health via `useServiceHealth` hook.

### Person 2 — Frontend (Issue Reporting & API Integration)
- [x] Build core issue reporting workflow in `pages/ReportIssue.jsx` and `components/reports/ReportForm.jsx`.
- [x] Implement file upload control with drag-and-drop, client-side MIME check, and image preview.
- [x] Add optional GPS geolocation detection with explicit user permission prompt.
- [x] Wire up multipart `FormData` via `services/aiService.js` to `POST /api/reports/analyze`.
- [x] Render real AI response in `components/reports/AnalysisCard.jsx` with editable complaint draft.
- [x] Implement `MyReports.jsx` to list, search, filter, edit drafts, and delete saved reports.
- [x] Build `ReportDetails.jsx` for comprehensive grievance view and status updating.
- [x] Build `About.jsx` covering ethical AI disclaimers and limitations.

### Person 3 — Backend (Controllers, DTOs & Validation)
- [x] Configure Spring Boot application with Maven in `backend/pom.xml`.
- [x] Implement `HealthController` for `GET /api/health`.
- [x] Implement `AiHealthController` for `GET /api/ai/health` with graceful offline fallback.
- [x] Implement `ReportController` for `POST /api/reports/analyze` forwarding multipart files to FastAPI.
- [x] Configure CORS in `config/CorsConfig.java` to whitelist `http://localhost:5173`.
- [x] Enforce file validation (MIME types `image/jpeg`, `image/png`, `image/webp`, and 8MB size limit).
- [x] Implement centralized exception handler in `exception/GlobalExceptionHandler.java` (no stack trace leakage).

### Person 4 — Backend (Persistence, Report Management & Tests)
- [x] Design entity and repository interface in `model/Report.java` and `repository/ReportRepository.java`.
- [x] Implement thread-safe in-memory store in `repository/InMemoryReportRepository.java` with seed data.
- [x] Implement report CRUD and statistics in `service/ReportService.java`.
- [x] Implement `AiClientService.java` using Spring `RestTemplate` to forward multipart requests to FastAPI.
- [x] Write automated tests in `HealthControllerTest.java` and `ReportControllerTest.java`.
- [x] Document future PostgreSQL schema in `DATABASE_SCHEMA.md`.

### Person 5 — AI Engine (FastAPI, Google Gemma 4 & Demo Mode)
- [x] Build FastAPI service in `AI-engine/app/main.py`.
- [x] Implement `GET /health` endpoint returning service status and model configuration.
- [x] Create Pydantic data schemas in `app/schemas.py` for structured report output.
- [x] Implement municipal prompt template in `app/gemma_service.py` tailored for civic damage assessment.
- [x] Implement hosted Google Gemma 4 API client with configurable model (`GEMMA_MODEL=gemma-4-26b-a4b-it`).
- [x] Implement deterministic simulated Demo Mode in `app/gemma_service.py`.
- [x] Write automated tests in `test_health.py` and `test_analyzer.py`.
