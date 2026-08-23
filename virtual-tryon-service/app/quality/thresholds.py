from pydantic import BaseModel, Field

class QualityThresholds(BaseModel):
    overall_threshold: float = Field(default=0.85, description="Min overall quality threshold for pass")
    identity_threshold: float = Field(default=0.85, description="Min identity score threshold")
    garment_threshold: float = Field(default=0.80, description="Min garment score threshold")
    structure_threshold: float = Field(default=0.80, description="Min structural score threshold")
    artifact_threshold: float = Field(default=0.85, description="Min artifact score threshold")
