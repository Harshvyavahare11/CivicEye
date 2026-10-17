# CivicEye V1 — API Contract Specification

This document defines the formal REST API contract between the **React Frontend** (`:5173`), the **Spring Boot Backend** (`:8080`), and the **Python AI Engine** (`:8000`).

---

## 1. Architecture Flow

```text
Citizen Browser (React :5173)
        │
        ├── GET  /api/health
        ├── GET  /api/ai/health
        ├── POST /api/reports/analyze (multipart/form-data)
        ├── POST /api/reports
        ├── GET  /api/reports
        ├── GET  /api/reports/{id}
        ├── PUT  /api/reports/{id}
        ├── DELETE /api/reports/{id}
        ├── POST /api/reports/{id}/send
        ├── POST /api/webhooks/messaging/simulate
        └── GET  /api/reports/stats
        ▼
Spring Boot Backend (:8080)
        │
        ├── FollowUpScheduler (Background cron / fixed-delay)
        │       └── Automated follow-up dispatch
        │
        ├── InboundWebhookService (:8080/api/webhooks/messaging)
        │       ├── Meta WhatsApp Cloud API (GET challenge & POST JSON)
        │       ├── Twilio SMS (POST form-urlencoded)
        │       └── Local simulation (/simulate)
        │
        ├── MessagingService (Adapter pattern)
        │       ├── DevMockMessagingProvider (Simulation / Fallback)
        │       ├── TwilioMessagingProvider (REST SMS client)
        │       └── WhatsAppMessagingProvider (Meta Graph API client)
        │
        │ Forwarding /analyze & health check
        ▼
Python FastAPI AI Engine (:8000)
        │
        │ Hosted Gemma 4 API (https://generativelanguage.googleapis.com)
        ▼
Google Gemma 4 (gemma-4-26b-a4b-it)
```

---

## 2. Spring Boot Backend Endpoints (`:8080`)

### 2.1 Backend Health Check
- **Method**: `GET`
- **Path**: `/api/health`
- **Response `200 OK`**:
```json
{
  "status": "UP",
  "service": "CivicEye Backend"
}
```

---

### 2.2 AI Engine Proxy Health Check
- **Method**: `GET`
- **Path**: `/api/ai/health`
- **Response `200 OK`**:
```json
{
  "status": "UP",
  "service": "CivicEye AI Engine",
  "connected": true,
  "gemmaKeyConfigured": true,
  "model": "gemma-4-26b-a4b-it",
  "message": "AI Engine is online and Google Gemma 4 is configured."
}
```

---

### 2.3 Analyze Issue Photo
- **Method**: `POST`
- **Path**: `/api/reports/analyze`
- **Content-Type**: `multipart/form-data`
- **Request Parameters**:
  - `image` *(MultipartFile, Required)*: JPEG, PNG, or WebP. Max 8 MB.
  - `description` *(String, Optional)*: User-supplied notes or context.
  - `location` *(String, Optional)*: Citizen-confirmed landmark or GPS.
  - `demoMode` *(Boolean, Optional, default: false)*: Set to true to receive deterministic simulated AI response.

#### Success Response `200 OK`
```json
{
  "category": "Pothole",
  "title": "Severe road crater on vehicular lane",
  "observations": [
    "Cracked and eroded bituminous surface layer",
    "Depression in road pavement with exposed sub-base aggregate"
  ],
  "severity": "High",
  "severityReason": "Visible cratering in vehicular lane presents imminent crash risk",
  "suggestedDepartment": "Roads & Bridges Department",
  "uncertainties": [
    "Accurate crater depth cannot be gauged from 2D photograph alone"
  ],
  "recommendedActions": [
    "Perform cold-mix or hot-mix asphalt patching on the affected road section"
  ],
  "complaintText": "A hazardous road crater has emerged on MG Road near Metro Pillar 42...",
  "demoMode": false
}
```

---

### 2.4 Create & Persist Report
- **Method**: `POST`
- **Path**: `/api/reports`
- **Content-Type**: `application/json`
- **Request Body**:
```json
{
  "title": "Severe pothole on Karve Road",
  "category": "Pothole",
  "description": "Deep depression near junction",
  "location": "Near Nal Stop, Karve Road, Pune",
  "latitude": 18.5204,
  "longitude": 73.8567,
  "complaintText": "A hazardous pothole has developed on the roadway...",
  "severity": "High",
  "severityReason": "Crater depth in vehicular lane",
  "observations": ["Cracked asphalt layer"],
  "suggestedDepartment": "Roads & Bridges Department",
  "uncertainties": ["Exact depth requires physical inspection"],
  "recommendedActions": ["Patch road surface"],
  "status": "Ready for Submission",
  "recipientPhone": "+15551234567",
  "demoMode": false,
  "imageUrl": "blob:http://..."
}
```
- **Response `201 Created`**: Returns created `Report` object.

---

### 2.5 Send Complaint via Messaging Adapter
- **Method**: `POST`
- **Path**: `/api/reports/{id}/send`
- **Content-Type**: `application/json`
- **Request Body** *(Optional)*:
```json
{
  "recipientPhone": "+15559876543",
  "customNote": "Urgent intervention requested"
}
```
- **Response `200 OK`**:
```json
{
  "reportId": "CE-2026-1002",
  "status": "SENT",
  "communicationStatus": "Sent",
  "providerMessageId": "MOCK-MSG-241465F2",
  "recipientPhone": "+15559876543",
  "messageBody": "📢 [CivicEye Grievance: CE-2026-1002]...",
  "providerName": "Development Simulation Adapter (Mock)",
  "delivered": false,
  "simulated": true,
  "sentAt": "2026-10-17T07:15:00Z",
  "statusMessage": "Message simulated in development mode (no external SMS quota consumed)."
}
```

---

### 2.6 Inbound Webhooks & Reply Keywords
- **Method**: `POST`
- **Path**: `/api/webhooks/messaging` (Live webhook for Twilio/WhatsApp)
- **Path**: `/api/webhooks/messaging/simulate` (Testing simulation endpoint)
- **Supported Keywords**:
  - `STATUS <REPORT_ID>`: Queries live lifecycle status, department, and timestamp.
  - `FIXED <REPORT_ID>`: Records citizen resolution confirmation; sets report to `Awaiting Citizen Confirmation`.
  - `NOT_FIXED <REPORT_ID>`: Escalates report to `Escalated` and dispatches alert to `civiceye.escalation.recipient`.
  - `HELP`: Returns bot commands instructions.
- **Simulation Request Body**:
```json
{
  "from": "+15551234567",
  "body": "FIXED CE-2026-1001",
  "reportId": "CE-2026-1001"
}
```
- **Response `200 OK`**:
```json
{
  "success": true,
  "reportId": "CE-2026-1001",
  "action": "MARKED_FIXED",
  "replyText": "Thank you! Your confirmation that CE-2026-1001 has been fixed has been recorded.",
  "newStatus": "Awaiting Citizen Confirmation"
}
```

---

### 2.7 List & Filter Reports
- **Method**: `GET`
- **Path**: `/api/reports`
- **Query Parameters**: `category`, `status`, `search`.

---

### 2.8 Get Report Details & Tracking
- **Method**: `GET`
- **Path**: `/api/reports/{id}`
- **Response**: Full report record including:
  - `status` (Lifecycle state)
  - `communicationStatus` (Messaging delivery state: Draft, Pending Send, Sent, Delivered, Failed)
  - `latitude`, `longitude`, `location`
  - `nextFollowUpAt`, `followUpSent`, `followUpHistory`
  - `inboundMessages` (Audit of citizen/recipient replies)
  - `escalationHistory` (Records of escalated grievances)
  - `citizenConfirmedResolved`

---

## 3. Python AI Engine Endpoints (`:8000`)

### 3.1 AI Health Check
- **Method**: `GET`
- **Path**: `/health`

### 3.2 Vision Analysis
- **Method**: `POST`
- **Path**: `/analyze`
- **Form Fields**: `image`, `description`, `location`, `demo_mode`.
