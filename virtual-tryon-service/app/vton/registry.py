from typing import Dict, Type
from app.vton.base import VTONEngine
from app.core.logging import logger

class VTONRegistry:
    _engines: Dict[str, Type[VTONEngine]] = {}

    @classmethod
    def register(cls, name: str):
        def decorator(engine_cls: Type[VTONEngine]):
            cls._engines[name.lower()] = engine_cls
            logger.info(f"Registered VTON Engine: {name}")
            return engine_cls
        return decorator

    @classmethod
    def get_engine_class(cls, name: str) -> Type[VTONEngine]:
        key = name.lower()
        if key not in cls._engines:
            raise KeyError(f"VTON Engine '{name}' not found in registry. Registered: {list(cls._engines.keys())}")
        return cls._engines[key]
