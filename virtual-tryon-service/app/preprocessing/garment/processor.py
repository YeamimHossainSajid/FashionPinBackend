from abc import ABC, abstractmethod
from typing import Tuple
from PIL import Image
import numpy as np
from app.api.schemas.virtual_try_on import GarmentCategory
from app.preprocessing.garment.segmentation import GarmentSegmenter

class GarmentCategoryStrategy(ABC):
    @abstractmethod
    def process(self, garment_img: Image.Image, target_size: Tuple[int, int]) -> Image.Image:
        pass

class UpperBodyStrategy(GarmentCategoryStrategy):
    def process(self, garment_img: Image.Image, target_size: Tuple[int, int]) -> Image.Image:
        cropped, _ = GarmentSegmenter.segment_and_crop(garment_img)
        return cropped.convert("RGB").resize(target_size, Image.Resampling.LANCZOS)

class LowerBodyStrategy(GarmentCategoryStrategy):
    def process(self, garment_img: Image.Image, target_size: Tuple[int, int]) -> Image.Image:
        cropped, _ = GarmentSegmenter.segment_and_crop(garment_img)
        return cropped.convert("RGB").resize(target_size, Image.Resampling.LANCZOS)

class DressStrategy(GarmentCategoryStrategy):
    def process(self, garment_img: Image.Image, target_size: Tuple[int, int]) -> Image.Image:
        cropped, _ = GarmentSegmenter.segment_and_crop(garment_img)
        return cropped.convert("RGB").resize(target_size, Image.Resampling.LANCZOS)

class FullBodyStrategy(GarmentCategoryStrategy):
    def process(self, garment_img: Image.Image, target_size: Tuple[int, int]) -> Image.Image:
        cropped, _ = GarmentSegmenter.segment_and_crop(garment_img)
        return cropped.convert("RGB").resize(target_size, Image.Resampling.LANCZOS)

class GarmentProcessor:
    _strategies = {
        GarmentCategory.UPPER_BODY: UpperBodyStrategy(),
        GarmentCategory.LOWER_BODY: LowerBodyStrategy(),
        GarmentCategory.DRESS: DressStrategy(),
        GarmentCategory.FULL_BODY: FullBodyStrategy(),
    }

    @classmethod
    def process(cls, garment_img: Image.Image, category: GarmentCategory, target_size: Tuple[int, int] = (768, 1024)) -> Image.Image:
        strategy = cls._strategies.get(category, UpperBodyStrategy())
        return strategy.process(garment_img, target_size)
