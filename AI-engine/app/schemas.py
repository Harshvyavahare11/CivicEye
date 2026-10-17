from typing import List, Literal, Optional
from pydantic import BaseModel, Field

SeverityLevel = Literal["Low", "Medium", "High", "Critical"]

class HealthStatusResponse(BaseModel):
    status: str = Field(default="UP", description="Service health status")
    service: str = Field(default="CivicEye AI Engine", description="Service name")
    gemma_api_key_configured: bool = Field(description="Whether a Gemma API key is configured")
    model: str = Field(description="Configured Gemma 4 model name")
    demo_mode_available: bool = Field(default=True, description="Whether demo simulation mode is available")

class IssueAnalysisResponse(BaseModel):
    category: str = Field(..., description="Detected category e.g. Pothole, Garbage, Drainage, Streetlight, Waterlogging, Sidewalk, Sanitation, Other")
    title: str = Field(..., description="Short descriptive title of the civic issue")
    observations: List[str] = Field(default_factory=list, description="Concrete visual observations extracted from the image")
    severity: SeverityLevel = Field(..., description="Preliminary severity assessment: Low, Medium, High, or Critical")
    severityReason: str = Field(..., description="Brief rationale explaining the preliminary severity")
    suggestedDepartment: str = Field(..., description="Recommended municipal department or agency to address the issue")
    uncertainties: List[str] = Field(default_factory=list, description="Uncertainties or factors requiring physical municipal inspection")
    recommendedActions: List[str] = Field(default_factory=list, description="Recommended immediate or next steps for citizens or authorities")
    complaintText: str = Field(..., description="Clear, concise, professional complaint draft ready for citizen review and editing")
    demoMode: bool = Field(default=False, description="Flag indicating if this result was produced by simulated demo mode")

class ErrorDetailResponse(BaseModel):
    error: str
    message: str
    detail: Optional[str] = None
