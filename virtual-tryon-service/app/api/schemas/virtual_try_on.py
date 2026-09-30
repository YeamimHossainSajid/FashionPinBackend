from enum import Enum
from typing import Dict, List, Optional
from pydantic import BaseModel, Field, HttpUrl, field_validator
import re

class GarmentCategory(str, Enum):
    UPPER_BODY = "UPPER_BODY"
    LOWER_BODY = "LOWER_BODY"
    DRESS = "DRESS"
    FULL_BODY = "FULL_BODY"
    TOPS = "tops"
    BOTTOMS = "bottoms"
    ONE_PIECES = "one-pieces"

class JobStatus(str, Enum):
    QUEUED = "QUEUED"
    VALIDATING = "VALIDATING"
    PREPROCESSING = "PREPROCESSING"
    GENERATING = "GENERATING"
    QUALITY_CHECK = "QUALITY_CHECK"
    RETRYING = "RETRYING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"

class TryOnConfig(BaseModel):
    seed: Optional[int] = Field(default=42, description="Random seed for deterministic generation")
    num_inference_steps: int = Field(default=30, ge=10, le=100, description="Diffusion steps")
    guidance_scale: float = Field(default=1.5, ge=1.0, le=10.0, description="CFG scale")
    denoise_strength: float = Field(default=1.0, ge=0.1, le=1.0, description="Denoising strength")
    garment_photo_type: str = Field(default="model", description="Garment photo type: 'model' or 'flat-lay'")
    preserve_face: bool = Field(default=True, description="Preserve person facial identity")
    preserve_hair: bool = Field(default=True, description="Preserve person hair region")
    preserve_hands: bool = Field(default=True, description="Preserve person hands & arms")
    output_format: str = Field(default="PNG", description="Output format (PNG/JPEG/WEBP)")

    @field_validator("output_format")
    @classmethod
    def validate_output_format(cls, v: str) -> str:
        norm = v.strip().upper()
        if norm not in ("PNG", "JPEG", "JPG", "WEBP"):
            raise ValueError(f"Unsupported output format '{v}'. Must be one of PNG, JPEG, WEBP.")
        return norm

    @field_validator("garment_photo_type")
    @classmethod
    def validate_garment_photo_type(cls, v: str) -> str:
        norm = v.strip().lower().replace("_", "-")
        if norm not in ("model", "flat-lay", "ghost-mannequin"):
            raise ValueError(f"Invalid garment_photo_type '{v}'. Must be 'model' or 'flat-lay'.")
        return norm

class CreateVTONJobRequest(BaseModel):
    person_image_url: str = Field(..., description="URL or key of person image")
    garment_image_url: str = Field(..., description="URL or key of garment image")
    garment_category: GarmentCategory = Field(default=GarmentCategory.UPPER_BODY, description="Garment category")
    config: Optional[TryOnConfig] = Field(default_factory=TryOnConfig)

    @field_validator("person_image_url", "garment_image_url")
    @classmethod
    def validate_image_url(cls, v: str) -> str:
        val = v.strip()
        if not val:
            raise ValueError("Image URL or storage key must not be empty.")
        if len(val) > 2048:
            raise ValueError("Image URL is too long (maximum 2048 characters).")
        return val

class QualityScoreResponse(BaseModel):
    overall_score: float = Field(..., description="Overall composite quality score")
    identity_score: float = Field(..., description="Face/identity preservation score")
    garment_score: float = Field(..., description="Garment fidelity score")
    structure_score: float = Field(..., description="Body structural placement score")
    artifact_score: float = Field(..., description="Artifact-free confidence score")

class VTONJobResponse(BaseModel):
    job_id: str
    status: JobStatus
    result_url: Optional[str] = None
    model_version: Optional[str] = None
    pipeline_version: Optional[str] = None
    quality_score: Optional[float] = None
    scores: Optional[QualityScoreResponse] = None
    attempt_count: int = 1
    failure_reason: Optional[str] = None
    created_at: str
    updated_at: str
