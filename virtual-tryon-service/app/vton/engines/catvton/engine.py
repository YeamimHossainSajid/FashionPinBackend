from typing import Dict, Any, Union
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
        config: Union[TryOnConfig, Dict[str, Any], None] = None
    ) -> VTONOutput:
        if not self._is_ready:
            self.initialize()

        if isinstance(config, dict):
            cfg_obj = TryOnConfig(**config)
        elif isinstance(config, TryOnConfig):
            cfg_obj = config
        else:
            cfg_obj = TryOnConfig()

        result_image = CatVTONInference.run_inference(
            self._pipeline_meta,
            person_data,
            garment_data,
            cfg_obj
        )

        return VTONOutput(
            generated_image=result_image,
            model_name="catvton",
            model_version=settings.model_version,
            pipeline_version=settings.pipeline_version,
            metadata={
                "seed": cfg_obj.seed,
                "num_inference_steps": cfg_obj.num_inference_steps,
                "guidance_scale": cfg_obj.guidance_scale,
                "denoise_strength": cfg_obj.denoise_strength,
                "device": self._pipeline_meta["device"],
            }
        )

    def is_ready(self) -> bool:
        return self._is_ready
