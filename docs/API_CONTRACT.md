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
        └── DELETE /api/reports/{id}
        ▼
Spring Boot Backend (:8080)
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
  "title": "Damaged asphalt surface with cratering",
  "observations": [
    "Cracked and eroded bituminous surface layer",
    "Depression in road pavement with exposed sub-base aggregate",
    "Irregular perimeter indicating progressive deterioration"
  ],
  "severity": "High",
  "severityReason": "Visible cratering in vehicular lane presents imminent crash risk to two-wheelers and cyclists",
  "suggestedDepartment": "Roads & Bridges Department",
  "uncertainties": [
    "Accurate crater depth cannot be gauged from 2D photograph alone",
    "Underlying utility line condition beneath asphalt requires ground inspection"
  ],
  "recommendedActions": [
    "Perform cold-mix or hot-mix asphalt patching on the affected road section",
    "Place cautionary high-visibility signage until repair completion"
  ],
  "complaintText": "A hazardous pothole has developed on the roadway near Nal Stop, Karve Road. Please inspect and patch the damaged section promptly to avoid vehicular accidents.",
  "demoMode": false
}
```

#### Error Responses
- `400 Bad Request`: Empty image file or invalid parameters.
- `413 Payload Too Large`: Uploaded image exceeds 8 MB.
- `415 Unsupported Media Type`: Unsupported MIME type (not JPEG, PNG, or WebP).
- `503 Service Unavailable`: AI engine offline or unreachable.

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
  "complaintText": "A hazardous pothole has developed on the roadway...",
  "severity": "High",
  "severityReason": "Crater depth in vehicular lane",
  "observations": ["Cracked asphalt layer"],
  "suggestedDepartment": "Roads & Bridges Department",
  "uncertainties": ["Exact depth requires physical inspection"],
  "recommendedActions": ["Patch road surface"],
  "status": "Ready for Submission",
  "demoMode": false,
  "imageUrl": "blob:http://..."
}
```
- **Response `201 Created`**: Returns created `Report` object with generated `id`, `createdAt`, `updatedAt`.

---

### 2.5 List & Filter Reports
- **Method**: `GET`
- **Path**: `/api/reports`
- **Query Parameters**:
  - `category` (optional): Filter by category (`Pothole`, `Garbage`, etc.)
  - `status` (optional): Filter by status (`Draft`, `Ready for Submission`, `Submitted`, `Resolved`)
  - `search` (optional): Search query matched against title, description, location, or ID
- **Response `200 OK`**: Array of `Report` objects sorted by `createdAt DESC`.

---

### 2.6 Get Report Details
- **Method**: `GET`
- **Path**: `/api/reports/{id}`
- **Response `200 OK`**: Report object or `404 Not Found`.

---

### 2.7 Update Report
- **Method**: `PUT`
- **Path**: `/api/reports/{id}`
- **Request Body**:
```json
{
  "title": "Updated Title",
  "category": "Pothole",
  "description": "Updated citizen notes",
  "location": "Updated location",
  "complaintText": "Updated grievance draft",
  "status": "Submitted"
}
```
- **Response `200 OK`**: Updated Report object.

---

### 2.8 Delete Report
- **Method**: `DELETE`
- **Path**: `/api/reports/{id}`
- **Response `204 No Content`** or `404 Not Found`.

---

### 2.9 Real Dashboard Statistics
- **Method**: `GET`
- **Path**: `/api/reports/stats`
- **Response `200 OK`**:
```json
{
  "totalReports": 3,
  "byStatus": {
    "Draft": 0,
    "Ready for Submission": 1,
    "Submitted": 1,
    "Resolved": 1
  },
  "byCategory": {
    "Pothole": 2,
    "Garbage": 1
  }
}
```

---

## 3. Python AI Engine Endpoints (`:8000`)

### 3.1 AI Health
- **Method**: `GET`
- **Path**: `/health`
- **Response `200 OK`**:
```json
{
  "status": "UP",
  "service": "CivicEye AI Engine",
  "gemma_api_key_configured": true,
  "model": "gemma-4-26b-a4b-it",
  "demo_mode_available": true
}
```

### 3.2 Vision Analysis
- **Method**: `POST`
- **Path**: `/analyze`
- **Form Fields**: `image` (binary file), `description` (text), `location` (text), `demo_mode` (boolean).
- **Response `200 OK`**: Matches `IssueAnalysisResponse` schema.
