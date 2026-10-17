import os
import logging
from typing import Optional
from dotenv import load_dotenv
from fastapi import FastAPI, UploadFile, File, Form, Query, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.schemas import HealthStatusResponse, IssueAnalysisResponse, ErrorDetailResponse
from app.validators import validate_image_file
from app.gemma_service import GemmaService

# Load environment variables from .env
load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("civiceye.ai-engine")

app = FastAPI(
    title="CivicEye AI Engine",
    description="FastAPI service analyzing civic issue photographs via hosted Google Gemma 4 API",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS setup
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

gemma_service = GemmaService()

@app.get(
    "/health",
    response_model=HealthStatusResponse,
    summary="AI Engine Health Check",
    tags=["System"]
)
async def health_check():
    """
    Returns health status of the AI Engine and whether Gemma API key is configured.
    Never exposes secrets or credentials.
    """
    return HealthStatusResponse(
        status="UP",
        service="CivicEye AI Engine",
        gemma_api_key_configured=gemma_service.is_api_key_configured(),
        model=gemma_service.get_model_name(),
        demo_mode_available=True
    )

@app.post(
    "/analyze",
    response_model=IssueAnalysisResponse,
    responses={
        400: {"model": ErrorDetailResponse, "description": "Invalid input or empty file"},
        413: {"model": ErrorDetailResponse, "description": "Image exceeds 8 MB"},
        415: {"model": ErrorDetailResponse, "description": "Unsupported image format"},
        429: {"model": ErrorDetailResponse, "description": "Gemma API quota / rate limit exceeded"},
        500: {"model": ErrorDetailResponse, "description": "AI analysis failure"},
        504: {"model": ErrorDetailResponse, "description": "Gemma API timeout"}
    },
    summary="Analyze Civic Issue Image",
    tags=["Analysis"]
)
async def analyze_image(
    image: UploadFile = File(..., description="Civic issue photo (JPEG, PNG, WebP; max 8 MB)"),
    description: Optional[str] = Form(None, description="Optional citizen description of the problem"),
    location: Optional[str] = Form(None, description="Optional citizen-confirmed location or landmark"),
    demo_mode: Optional[bool] = Form(False, description="Enable simulated demo mode")
):
    """
    Accepts multipart/form-data upload, validates file parameters,
    and analyzes the civic problem using Google Gemma 4.
    """
    # 1. Validate image constraints (size, MIME type)
    image_bytes = await validate_image_file(image)
    mime_type = (image.content_type or "image/jpeg").lower().strip()

    # 2. Invoke Gemma or simulated demo mode
    try:
        result = await gemma_service.analyze_with_gemma(
            image_bytes=image_bytes,
            mime_type=mime_type,
            description=description,
            location=location,
            filename=image.filename,
            demo_mode_requested=bool(demo_mode)
        )
        return result
    except PermissionError as pe:
        logger.warning(f"Auth error: {str(pe)}")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=str(pe))
    except ResourceWarning as rw:
        logger.warning(f"Quota error: {str(rw)}")
        raise HTTPException(status_code=status.HTTP_429_TOO_MANY_REQUESTS, detail=str(rw))
    except TimeoutError as te:
        logger.error(f"Timeout calling Gemma: {str(te)}")
        raise HTTPException(status_code=status.HTTP_504_GATEWAY_TIMEOUT, detail=str(te))
    except ValueError as ve:
        logger.error(f"Validation/Model error: {str(ve)}")
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))
    except RuntimeError as re:
        logger.error(f"Runtime error: {str(re)}")
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(re))
    except Exception as e:
        logger.exception("Unexpected error in /analyze")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An error occurred during AI analysis. Please try again or switch to Demo Mode."
        )

if __name__ == "__main__":
    import uvicorn
    host = os.getenv("AI_HOST", "127.0.0.1")
    port = int(os.getenv("AI_PORT", 8000))
    uvicorn.run("app.main:app", host=host, port=port, reload=True)
