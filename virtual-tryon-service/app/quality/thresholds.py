import os
from pydantic import Field, ConfigDict
from pydantic_settings import BaseSettings

class QualityThresholds(BaseSettings):
    model_config = ConfigDict(env_prefix="VTON_QUALITY_", env_file=".env", extra="ignore")

    overall_threshold: float = Field(default=0.85, description="Min overall quality threshold for pass")
    identity_threshold: float = Field(default=0.85, description="Min identity score threshold")
    garment_threshold: float = Field(default=0.80, description="Min garment score threshold")
    structure_threshold: float = Field(default=0.80, description="Min structural score threshold")
    artifact_threshold: float = Field(default=0.85, description="Min artifact score threshold")
