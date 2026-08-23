from typing import Dict, Any
from PIL import Image
from app.vton.base import VTONEngine, PersonData, GarmentData, VTONOutput
from app.vton.registry import VTONRegistry
from app.vton.engines.catvton.loader import CatVTONLoader
from app.vton.engines.catvton.inference import CatVTONInference
from app.api.schemas.virtual_try_on import TryOnConfig
from app.core.config import settings

@VTONRegistry.register("catvton")
class CatVTONEngine(VTONEngine):
    def __init__(self):
        self._pipeline_meta = None
        self._is_ready = False

    def initialize(self) -> None:
        self._pipeline_meta = CatVTONLoader.load_pipeline()
        self._is_ready = True

    def generate(
        self,
        person_data: PersonData,
        garment_data: GarmentData,
        config: TryOnConfig
    ) -> VTONOutput:
        if not self._is_ready:
            self.initialize()

        result_image = CatVTONInference.run_inference(
            self._pipeline_meta,
            person_data,
            garment_data,
            config
        )

        return VTONOutput(
            generated_image=result_image,
            model_name="catvton",
            model_version=settings.model_version,
            pipeline_version=settings.pipeline_version,
            metadata={
                "seed": config.seed,
                "num_inference_steps": config.num_inference_steps,
                "guidance_scale": config.guidance_scale,
                "denoise_strength": config.denoise_strength,
                "device": self._pipeline_meta["device"],
            }
        )

    def is_ready(self) -> bool:
        return self._is_ready
