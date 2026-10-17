# CivicEye — AI Engine (Python FastAPI & Hosted Google Gemma 4)

This service provides AI-powered civic issue image analysis using the hosted **Google Gemma 4 API** (configurable model: `gemma-4-26b-a4b-it`) with full Pydantic schema validation, strict municipal prompt rules, and an explicit **Demo Mode** for simulation.

---

## 1. Prerequisites

- Python 3.11+ (tested on Python 3.14)
- Google GenAI / Gemma API Key (optional for Demo Mode)

---

## 2. Setup & Installation

From within the `AI-engine` directory:

```powershell
# Create a virtual environment (optional but recommended)
python -m venv .venv
.\.venv\Scripts\Activate.ps1

# Install required dependencies
pip install -r requirements.txt
```

---

## 3. Configuration

Copy `.env.example` to `.env`:

```powershell
Copy-Item .env.example .env
```

Configure your environment variables:

| Variable | Default | Description |
| :--- | :--- | :--- |
| `AI_HOST` | `127.0.0.1` | Server host |
| `AI_PORT` | `8000` | Server listening port |
| `GEMMA_API_KEY` | *(empty)* | Your Google Gemma 4 API key |
| `GEMMA_MODEL` | `gemma-4-26b-a4b-it` | Target Gemma model |
| `DEMO_MODE_DEFAULT` | `false` | Fallback to demo mode if key is unconfigured |

---

## 4. Running the Service

```powershell
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

Swagger API documentation is accessible at:
- **Swagger UI**: [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)
- **ReDoc**: [http://127.0.0.1:8000/redoc](http://127.0.0.1:8000/redoc)

---

## 5. Endpoints

- `GET /health`: Health check and Gemma key configuration status.
- `POST /analyze`: Multipart form upload (`image`, optional `description`, optional `location`, optional `demo_mode`).

---

## 6. Running Tests

```powershell
pytest
```
