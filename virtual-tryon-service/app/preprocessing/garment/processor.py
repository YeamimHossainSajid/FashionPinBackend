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

    @staticmethod
    def _fit_to_canvas(
        garment_rgba: Image.Image,
        target_size: Tuple[int, int],
        max_width_ratio: float = 0.85,
        max_height_ratio: float = 0.85,
        vertical_anchor: str = "center",
    ) -> Image.Image:
        """
        Preserves garment aspect ratio and places it on a target canvas with category-aware alignment.
        """
        target_w, target_h = target_size
        gw, gh = garment_rgba.size

        if gw == 0 or gh == 0:
            return Image.new("RGB", target_size, (255, 255, 255))

        # Determine scale preserving aspect ratio
        max_allowed_w = target_w * max_width_ratio
        max_allowed_h = target_h * max_height_ratio
        scale = min(max_allowed_w / gw, max_allowed_h / gh)

        new_w = max(1, int(gw * scale))
        new_h = max(1, int(gh * scale))

        scaled_garment = garment_rgba.resize((new_w, new_h), Image.Resampling.LANCZOS)

        # Calculate canvas offsets
        offset_x = (target_w - new_w) // 2

        if vertical_anchor == "top":
            offset_y = int(target_h * 0.08)
        elif vertical_anchor == "bottom":
            offset_y = target_h - new_h - int(target_h * 0.08)
        else:  # center
            offset_y = (target_h - new_h) // 2

        # Clamp offsets
        offset_x = max(0, min(target_w - new_w, offset_x))
        offset_y = max(0, min(target_h - new_h, offset_y))

        # Create white background canvas and paste with alpha mask
        canvas = Image.new("RGBA", target_size, (255, 255, 255, 255))
        if scaled_garment.mode == "RGBA":
            canvas.paste(scaled_garment, (offset_x, offset_y), scaled_garment)
        else:
            canvas.paste(scaled_garment, (offset_x, offset_y))

        return canvas.convert("RGB")

class UpperBodyStrategy(GarmentCategoryStrategy):
    def process(self, garment_img: Image.Image, target_size: Tuple[int, int]) -> Image.Image:
        cropped_rgba, _ = GarmentSegmenter.segment_and_crop(garment_img)
        return self._fit_to_canvas(
            cropped_rgba,
            target_size,
            max_width_ratio=0.88,
            max_height_ratio=0.62,
            vertical_anchor="top"
        )

class LowerBodyStrategy(GarmentCategoryStrategy):
    def process(self, garment_img: Image.Image, target_size: Tuple[int, int]) -> Image.Image:
        cropped_rgba, _ = GarmentSegmenter.segment_and_crop(garment_img)
        return self._fit_to_canvas(
            cropped_rgba,
            target_size,
            max_width_ratio=0.82,
            max_height_ratio=0.68,
            vertical_anchor="bottom"
        )

class DressStrategy(GarmentCategoryStrategy):
    def process(self, garment_img: Image.Image, target_size: Tuple[int, int]) -> Image.Image:
        cropped_rgba, _ = GarmentSegmenter.segment_and_crop(garment_img)
        return self._fit_to_canvas(
            cropped_rgba,
            target_size,
            max_width_ratio=0.88,
            max_height_ratio=0.88,
            vertical_anchor="center"
        )

class FullBodyStrategy(GarmentCategoryStrategy):
    def process(self, garment_img: Image.Image, target_size: Tuple[int, int]) -> Image.Image:
        cropped_rgba, _ = GarmentSegmenter.segment_and_crop(garment_img)
        return self._fit_to_canvas(
            cropped_rgba,
            target_size,
            max_width_ratio=0.88,
            max_height_ratio=0.90,
            vertical_anchor="center"
        )

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
