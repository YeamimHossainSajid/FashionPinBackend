from typing import Optional
from PIL import Image
import torch
import logging

from app.vton.base import VTONEngine, PersonData, GarmentData, VTONOutput
from app.vton.registry import VTONRegistry
from app.core.config import settings
from app.core.logging import logger
from .pipeline import TryOnPipeline


@VTONRegistry.register("fashn-vton")
@VTONRegistry.register("fashn")
class FashnVTONEngine(VTONEngine):
    """
    Official FASHN VTON v1.5 Engine implementation for Fashion Pin.
    Integrates FASHN MMDiT Try-On model, DWPose ONNX keypoints, and FashnHumanParser.
    """

    def __init__(self, model_path: Optional[str] = None, device: Optional[str] = None):
        self.model_path = model_path or settings.VTON_MODEL_PATH
        self.device = device or settings.VTON_DEVICE
        self.pipeline = None
        self._ready = False

    def initialize(self) -> None:
        logger.info(f"Initializing FashnVTONEngine from weights_dir='{self.model_path}', device='{self.device}'")
        try:
            self.pipeline = TryOnPipeline(weights_dir=self.model_path, device=self.device)
            self._ready = True
            logger.info("FashnVTONEngine initialized successfully and ready.")
        except Exception as e:
            logger.error(f"Failed to initialize FashnVTONEngine: {e}")
            self._ready = False
            raise e

    def is_ready(self) -> bool:
        return self._ready and self.pipeline is not None

    def generate(
        self,
        person_data: PersonData,
        garment_data: GarmentData,
        config: Optional[dict] = None
    ) -> VTONOutput:
        if not self.is_ready():
            self.initialize()

        cfg = config or {}
        raw_cat = str(garment_data.category.value if hasattr(garment_data.category, "value") else garment_data.category).lower()
        cat_map = {
            "upper_body": "tops",
            "tops": "tops",
            "top": "tops",
            "lower_body": "bottoms",
            "bottoms": "bottoms",
            "bottom": "bottoms",
            "dress": "one-pieces",
            "full_body": "one-pieces",
            "one-pieces": "one-pieces",
            "one_piece": "one-pieces",
        }
        fashn_cat = cat_map.get(raw_cat, "tops")
        garment_photo_type = cfg.get("garment_photo_type", "model")
        num_timesteps = cfg.get("num_inference_steps", cfg.get("num_timesteps", 30))
        guidance_scale = cfg.get("guidance_scale", 1.5)
        seed = cfg.get("seed", 42)

        pipeline_out = self.pipeline(
            person_image=person_data.image,
            garment_image=garment_data.image,
            category=fashn_cat,
            garment_photo_type=garment_photo_type,
            num_timesteps=num_timesteps,
            guidance_scale=guidance_scale,
            seed=seed,
        )

        final_img = pipeline_out.images[0]
        return VTONOutput(
            generated_image=final_img,
            model_name="fashn-vton",
            model_version="1.5",
            pipeline_version="1.0.0",
            metadata={
                "category": fashn_cat,
                "garment_photo_type": garment_photo_type,
                "num_timesteps": num_timesteps,
                "guidance_scale": guidance_scale,
                "seed": seed,
            }
        )
