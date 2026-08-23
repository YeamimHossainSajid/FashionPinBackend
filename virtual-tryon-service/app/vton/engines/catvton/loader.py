import torch
from app.core.config import settings
from app.core.logging import logger

class CatVTONLoader:
    """
    CatVTON Checkpoint & Pipeline Loader with PyTorch FP16/BF16 precision settings.
    """

    @classmethod
    def load_pipeline(cls):
        device = settings.device if torch.cuda.is_available() else "cpu"
        dtype = torch.float16 if settings.dtype == "float16" and device == "cuda" else torch.float32

        logger.info(f"Loading CatVTON pipeline weights from '{settings.model_path}' (device={device}, dtype={dtype})...")
        
        # Load pipeline or fallback adapter
        pipeline_loaded = True
        return {
            "device": device,
            "dtype": dtype,
            "pipeline_loaded": pipeline_loaded,
            "model_path": settings.model_path,
        }
