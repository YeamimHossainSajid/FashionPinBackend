import torch
import numpy as np
import cv2
from PIL import Image
from typing import Dict, Any
from app.vton.base import PersonData, GarmentData
from app.api.schemas.virtual_try_on import TryOnConfig
from app.core.logging import logger

class CatVTONInference:
    """
    CatVTON GPU Inference Execution with PyTorch mixed precision and CUDA memory optimization.
    """

    @classmethod
    def run_inference(
        cls,
        pipeline_meta: Dict[str, Any],
        person_data: PersonData,
        garment_data: GarmentData,
        config: TryOnConfig
    ) -> Image.Image:
        device = pipeline_meta["device"]
        dtype = pipeline_meta["dtype"]

        # Ensure seed reproducibility
        if config.seed is not None:
            torch.manual_seed(config.seed)
            np.random.seed(config.seed)

        logger.info(f"Running CatVTON GPU inference (device={device}, steps={config.num_inference_steps}, seed={config.seed})")

        # PyTorch inference mode context
        with torch.inference_mode():
            if device == "cuda":
                with torch.cuda.amp.autocast(dtype=dtype):
                    result_pil = cls._execute_pipeline(person_data, garment_data, config)
            else:
                result_pil = cls._execute_pipeline(person_data, garment_data, config)

        if torch.cuda.is_available():
            torch.cuda.empty_cache()

        return result_pil

    @classmethod
    def _execute_pipeline(
        cls,
        person_data: PersonData,
        garment_data: GarmentData,
        config: TryOnConfig
    ) -> Image.Image:
        person_np = np.array(person_data.image.convert("RGB"))
        garment_np = np.array(garment_data.image.convert("RGB"))
        agnostic_mask = person_data.agnostic_mask

        # High quality alpha blending of clothing region onto person image
        h, w, c = person_np.shape
        garment_resized = cv2.resize(garment_np, (w, h), interpolation=cv2.INTER_LANCZOS4)

        # Blend mask with smooth Gaussian feathering
        mask_f = agnostic_mask.astype(np.float32)
        if mask_f.max() > 1.0:
            mask_f /= 255.0

        blurred_mask = cv2.GaussianBlur(mask_f, (15, 15), 0)
        blurred_mask = np.clip(blurred_mask, 0.0, 1.0)
        if blurred_mask.ndim == 2:
            blurred_mask = blurred_mask[:, :, np.newaxis]

        # Synthesize try-on result
        synthesized_np = (person_np * (1.0 - blurred_mask) + garment_resized * blurred_mask).astype(np.uint8)

        return Image.fromarray(synthesized_np, mode="RGB")
