# CivicEye — AI-Powered Civic Issue Reporting & Follow-Up Platform (V1)

CivicEye empowers citizens to photograph, analyze, and report visible civic infrastructure problems (such as potholes, open drains, garbage dumps, broken streetlights, and waterlogging) using computer vision and hosted **Google Gemma 4 AI**.

The platform provides an end-to-end civic accountability loop:
1. **Interactive Reporting**: Photo upload + device camera capture with interactive Leaflet / OpenStreetMap GPS pin-drop and Nominatim reverse geocoding.
2. **Gemma 4 AI Analysis**: Categorizes issues, assesses preliminary severity with physical rationale, identifies jurisdictional department, and drafts verified complaint text.
3. **Complaint Messaging Adapter**: Dispatches grievance notifications to test or municipal contacts via Twilio SMS, WhatsApp Business Cloud API, or local development simulation.
4. **Dedicated Tracking**: Real-time lifecycle vs communication status, delivery receipts, and follow-up schedules.
5. **Backend Scheduled Follow-Ups**: Automated background scheduler checks open reports and dispatches periodic follow-up status reminders.
6. **Two-Way Citizen Resolution & Escalation**: Inbound webhook processes SMS/WhatsApp replies (`STATUS`, `FIXED`, `NOT_FIXED`, `HELP`), updating issue lifecycle states and triggering escalations.

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
        ├── FollowUpScheduler (Automated backend follow-up tasks)
        ├── InboundWebhookService (:8080/api/webhooks/messaging)
        ├── MessagingService (Twilio / WhatsApp / Dev Mock Adapter)
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
Key settings in `AI-engine/.env`:
```env
AI_HOST=127.0.0.1
AI_PORT=8000
GEMMA_API_KEY=your_google_gemma_api_key_here
GEMMA_MODEL=gemma-4-26b-a4b-it
DEMO_MODE_DEFAULT=false
```

### C. Backend Configuration (`backend/src/main/resources/application.properties` or environment variables)
```env
# Messaging Provider: mock | twilio | whatsapp
civiceye.messaging.provider=mock
civiceye.messaging.test-recipient=+15551234567

# Backend Follow-Up Scheduler (Minutes between follow-up checks)
civiceye.follow-up.delay-minutes=2

# Escalation Authority Phone Contact
civiceye.escalation.recipient=+15559998888
```

---

## 4. Running the Application (3 Services)

Open three separate Windows PowerShell windows:

### Terminal 1: Python AI Engine (Port 8000)
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\AI-engine
pip install -r requirements.txt
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000
```
- Health Check: `GET http://127.0.0.1:8000/health`
- Interactive Swagger UI: `http://127.0.0.1:8000/docs`

### Terminal 2: Spring Boot Backend (Port 8080)
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\backend
mvn spring-boot:run "-Djava.net.preferIPv4Stack=true"
```
- Health Check: `GET http://localhost:8080/api/health`
- AI Proxy Health: `GET http://localhost:8080/api/ai/health`
- Report List: `GET http://localhost:8080/api/reports`
- Webhook Simulate: `POST http://localhost:8080/api/webhooks/messaging/simulate`

### Terminal 3: React Frontend (Port 5173)
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\Frontend
npm install
npm run dev
```
Open your browser and navigate to: **`http://localhost:5173`**

---

## 5. End-to-End User Flow (Stages A to J)

1. **Stage A — Animated Splash Screen**:
   - Opens on fresh page load with subtle civic-tech styling (navy, white, teal accents).
   - Automatically transitions to the dashboard after 1.8s (or immediate if `prefers-reduced-motion` is active).
2. **Stage B — Home Dashboard**:
   - Real statistics backed by repository records (Total Reports, Dispatched Messages, Escalated, Resolved).
   - "Report an Issue" and "Track Issue" call-to-actions.
   - Interactive 5-step civic accountability explanation.
3. **Stage C — Report an Issue**:
   - Upload file (JPEG, PNG, WebP up to 8 MB) or capture directly with device camera.
   - Interactive OpenStreetMap (Leaflet) with draggable pin and automated Nominatim reverse geocoding to address.
   - Test recipient phone configuration.
4. **Stage D — Gemma 4 Vision Analysis**:
   - Categorizes problem, computes preliminary severity rationale, assigns jurisdictional department, identifies verification factors, and creates editable complaint text.
5. **Stage E — Dispatch Complaint**:
   - Review draft and click "Send Complaint via WhatsApp / SMS".
   - Confirmation dialog with destination phone and civic disclaimer.
   - Sets communication status (`Sent` / `Delivered`) and returns provider message ID.
6. **Stage F — Track Issue (`/track` & `/track/:id`)**:
   - Search by Report ID.
   - Shows lifecycle status vs messaging delivery status.
   - Confirmed location map, complaint quote, delivery receipt, and follow-up schedule.
7. **Stage G/I/J — Two-Way WhatsApp/SMS Bot & Reply Simulation**:
   - Interactive reply simulator right on the Track Issue page!
   - Test keywords:
     - `STATUS <ID>`: Queries report status.
     - `FIXED <ID>`: Confirms citizen resolution; stops follow-ups.
     - `NOT_FIXED <ID>`: Triggers escalation workflow to higher authority.
     - `HELP`: Shows bot commands.
8. **Stage H — Automated Scheduled Follow-Ups**:
   - Background Spring Boot scheduler (`@Scheduled`) periodically scans due reports and sends reminders.

---

## 6. Running Automated Tests

### Backend Unit & Integration Tests (33 tests)
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\backend
mvn test "-Djava.net.preferIPv4Stack=true"
```
Verifies:
- Report creation, listing, updating, and statistics.
- File validation (rejecting empty images and non-image types).
- Outgoing complaint dispatch via messaging adapter (`POST /api/reports/{id}/send`).
- Inbound webhook keywords (`STATUS`, `FIXED`, `NOT_FIXED`, `HELP`).
- Webhook deduplication and escalation triggers.
- **Twilio SMS Notifications Suite (`TwilioNotificationTest`, 12 tests)**:
  - Default mock mode functionality without credentials.
  - Safe error handling when Twilio credentials are missing.
  - Proper payload construction (E.164 recipient, sender, concise body).
  - Provider response handling and message acceptance tracking.
  - Safe handling of authentication errors and API failure codes.
  - Transactional isolation: notification failure does not rollback complaints.
  - Idempotency and deduplication via `notification_logs`.
  - Complainant SMS consent enforcement.
  - Controlled test-recipient and escalation-recipient routing.
  - Follow-up scheduling delays and valid lifecycle status updates.

---

## 7. Twilio SMS Notifications Setup & Developer Guide

### A. Modes of Operation
CivicEye supports two primary messaging modes controlled by `CIVICEYE_MESSAGING_PROVIDER` in `.env`:

1. **Mock Mode (`mock`, Default)**:
   - Does not require any Twilio credentials.
   - All outgoing SMS notifications (submissions, assignments, status updates, follow-ups, and escalations) are logged directly to the backend console.
   - Safe for CI/CD pipelines, hackathon demonstrations, and offline development.

2. **Twilio Mode (`twilio`)**:
   - Uses the official Twilio Programmable Messaging API (`https://api.twilio.com/2010-04-01/Accounts/{AccountSid}/Messages.json`).
   - Requires `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, and `TWILIO_FROM_PHONE`.

### B. Configuring Twilio Credentials
In your local `.env` file (which is git-ignored):
```properties
CIVICEYE_MESSAGING_PROVIDER=twilio
CIVICEYE_TEST_RECIPIENT=+917058207967

TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_AUTH_TOKEN=your_twilio_auth_token
TWILIO_FROM_PHONE=+17372508034

CIVICEYE_FOLLOW_UP_DELAY_MINUTES=2
CIVICEYE_ESCALATION_RECIPIENT=+917058207967
```

### C. Twilio Console Walkthrough
1. **Sign Up / Log In**: Visit [https://console.twilio.com/](https://console.twilio.com/).
2. **Account SID & Auth Token**: Found directly on the dashboard home under "Account Info".
3. **Obtain an SMS Sender Number**:
   - Navigate to **Phone Numbers** -> **Manage** -> **Active Numbers** (or **Buy a number**).
   - Ensure the purchased number has "SMS" capabilities enabled.
   - Copy the number in E.164 format (e.g. `+17372508034`) to `TWILIO_FROM_PHONE`.
4. **Trial Account Verification**:
   - If using a Twilio Trial account, SMS can **only** be sent to phone numbers that have been verified.
   - Navigate to **Phone Numbers** -> **Manage** -> **Verified Caller IDs**.
   - Add and verify your personal test mobile number (e.g. `+917058207967`).
   - Set this number as `CIVICEYE_TEST_RECIPIENT` and `CIVICEYE_ESCALATION_RECIPIENT`.

### D. Safe Controlled Testing Instructions
To send a single controlled test message using curl:
```bash
curl -X POST "https://api.twilio.com/2010-04-01/Accounts/${TWILIO_ACCOUNT_SID}/Messages.json" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "To=%2B917058207967" \
  -d "From=%2B17372508034" \
  -d "Body=CivicEye+Test+Notification:+Issue+CE-2026-0001+recorded" \
  -u "${TWILIO_ACCOUNT_SID}:${TWILIO_AUTH_TOKEN}"
```

### E. Notification Audit Trail & Troubleshooting
All notification attempts are saved in the `notification_logs` PostgreSQL table:
- **`idempotency_key`**: Prevents duplicate SMS messages for the same report event.
- **`recipient_masked`**: Masks phone numbers (e.g. `+91****207967`) in database logs to protect user privacy.
- **`outcome`**: `ACCEPTED`, `SIMULATED`, or `FAILED`.
- **Common Error Codes**:
  - `TWILIO_ERR_20003`: Invalid Account SID or Auth Token.
  - `TWILIO_ERR_21608`: Unverified recipient phone number on a Twilio trial account.
  - `TWILIO_ERR_21211`: Invalid phone number format (must follow E.164 format `+[country][number]`).
  - `TWILIO_ERR_429`: Rate limit exceeded.

---

### AI Engine Tests (Pytest)
```powershell
cd c:\Users\aliba\Desktop\AIT\CivicEye\AI-engine
pytest
```
Verifies:
- AI service health and config flags.
- Image payload validation.
- Deterministic simulation in Demo Mode.

---

## 7. Two-Way Webhook Verification

For live Meta WhatsApp Cloud API webhooks:
- Webhook URL: `https://<YOUR_DOMAIN>/api/webhooks/messaging`
- Verification Token: Configured in `civiceye.messaging.whatsapp.verify-token`.

For Twilio SMS webhooks:
- In Twilio Console, configure your number's "A Message Comes In" Webhook to:
  `https://<YOUR_DOMAIN>/api/webhooks/messaging` (HTTP POST).
