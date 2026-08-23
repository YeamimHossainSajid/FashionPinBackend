from abc import ABC, abstractmethod
from typing import Dict, Any, Optional
from dataclasses import dataclass
import numpy as np
from PIL import Image
from app.api.schemas.virtual_try_on import GarmentCategory, TryOnConfig

@dataclass
class PersonData:
    image: Image.Image
    parse_map: np.ndarray
    densepose_map: np.ndarray
    agnostic_mask: np.ndarray
    protected_mask: np.ndarray

@dataclass
class GarmentData:
    image: Image.Image
    category: GarmentCategory
    mask: Optional[np.ndarray] = None

@dataclass
class VTONOutput:
    generated_image: Image.Image
    model_name: str
    model_version: str
    pipeline_version: str
    metadata: Dict[str, Any]

class VTONEngine(ABC):
    @abstractmethod
    def initialize(self) -> None:
        """Load model checkpoints and allocate GPU tensors."""
        pass

    @abstractmethod
    def generate(
        self,
        person_data: PersonData,
        garment_data: GarmentData,
        config: TryOnConfig
    ) -> VTONOutput:
        """Execute virtual try-on inference pipeline."""
        pass

    @abstractmethod
    def is_ready(self) -> bool:
        """Check if model is loaded into GPU memory and ready for inference."""
        pass
