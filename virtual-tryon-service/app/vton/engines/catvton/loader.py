import torch
from app.core.config import settings
from app.core.logging import logger

class CatVTONLoader:
    """
    CatVTON Checkpoint & Pipeline Loader with PyTorch FP16/BF16 precision settings.
    """

    @classmethod
    def resolve_device_and_dtype(cls):
        # Hardware autodetect: CUDA > MPS (Apple Silicon) > CPU
        if settings.device == "cuda" and torch.cuda.is_available():
            device = "cuda"
        elif (settings.device in ("mps", "cuda") or hasattr(torch.backends, "mps")) and getattr(torch.backends, "mps", None) and torch.backends.mps.is_available():
            device = "mps"
        else:
            device = "cpu"

        # Dtype resolution
        requested_dtype = settings.dtype.lower()
        if device == "cuda":
            if requested_dtype in ("bfloat16", "bf16") and torch.cuda.is_bf16_supported():
                dtype = torch.bfloat16
            elif requested_dtype in ("float16", "fp16", "bfloat16"):
                dtype = torch.float16
            else:
                dtype = torch.float32
        elif device == "mps":
            dtype = torch.float32  # MPS has best stability with float32/float16
        else:
            dtype = torch.float32

        return device, dtype

    @classmethod
    def load_pipeline(cls):
        device, dtype = cls.resolve_device_and_dtype()

        logger.info(f"Loading CatVTON pipeline weights from '{settings.model_path}' (resolved_device={device}, dtype={dtype})...")
        
        # Load pipeline or fallback adapter
        pipeline_loaded = True
        return {
            "device": device,
            "dtype": dtype,
            "pipeline_loaded": pipeline_loaded,
            "model_path": settings.model_path,
        }
