from typing import Optional, Dict, Any
import torch
from app.vton.base import VTONEngine
from app.vton.registry import VTONRegistry
from app.core.config import settings
from app.core.logging import logger

class ModelManager:
    _instance: Optional['ModelManager'] = None
    _engine: Optional[VTONEngine] = None

    def __init__(self):
        self.device = settings.device if torch.cuda.is_available() else "cpu"
        self.model_name = settings.model_name

    @classmethod
    def get_instance(cls) -> 'ModelManager':
        if cls._instance is None:
            cls._instance = ModelManager()
        return cls._instance

    def load(self) -> None:
        logger.info(f"Initializing VTON ModelManager for engine '{self.model_name}' on device '{self.device}'")
        try:
            engine_cls = VTONRegistry.get_engine_class(self.model_name)
            self._engine = engine_cls()
            self._engine.initialize()
            logger.info(f"VTON Engine '{self.model_name}' successfully loaded and ready.")
        except Exception as e:
            logger.error(f"Failed to load VTON Engine '{self.model_name}': {str(e)}", exc_info=True)
            raise e

    def get_engine(self) -> VTONEngine:
        if self._engine is None or not self._engine.is_ready():
            self.load()
        return self._engine

    def health(self) -> Dict[str, Any]:
        ready = self._engine is not None and self._engine.is_ready()
        gpu_available = torch.cuda.is_available()
        vram_allocated = torch.cuda.memory_allocated() if gpu_available else 0
        vram_reserved = torch.cuda.memory_reserved() if gpu_available else 0

        return {
            "ready": ready,
            "engine": settings.model_name,
            "model_version": settings.model_version,
            "pipeline_version": settings.pipeline_version,
            "device": self.device,
            "gpu_available": gpu_available,
            "vram_allocated_mb": round(vram_allocated / (1024 * 1024), 2),
            "vram_reserved_mb": round(vram_reserved / (1024 * 1024), 2),
        }

    def unload(self) -> None:
        if self._engine is not None:
            logger.info("Unloading VTON engine and clearing GPU memory")
            self._engine = None
            if torch.cuda.is_available():
                torch.cuda.empty_cache()
