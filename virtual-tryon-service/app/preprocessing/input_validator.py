import io
from typing import Tuple, Dict, Any, Optional
import cv2
import numpy as np
from PIL import Image, ImageOps
from app.core.exceptions import VTONException, ErrorCode
from app.core.logging import logger

class InputValidator:
    MIN_RESOLUTION: int = 256
    MAX_RESOLUTION: int = 4096
    MIN_LAPLACIAN_VAR: float = 10.0  # Blur detection threshold

    @classmethod
    def validate_image_bytes(cls, image_bytes: bytes, image_type: str = "person") -> Tuple[Image.Image, np.ndarray]:
        if not image_bytes or len(image_bytes) == 0:
            raise VTONException(
                code=ErrorCode.CORRUPTED_IMAGE,
                message=f"The {image_type} image file is empty or missing."
            )

        try:
            raw_img = Image.open(io.BytesIO(image_bytes))
            # Correct orientation from EXIF metadata (essential for smartphone photos)
            transposed_img = ImageOps.exif_transpose(raw_img)
            if transposed_img is not None:
                raw_img = transposed_img

            # Handle transparency (RGBA, LA, P with transparency) by compositing over clean white background
            if raw_img.mode in ("RGBA", "LA") or (raw_img.mode == "P" and "transparency" in raw_img.info):
                rgba_img = raw_img.convert("RGBA")
                background = Image.new("RGBA", rgba_img.size, (255, 255, 255, 255))
                composited = Image.alpha_composite(background, rgba_img)
                pil_img = composited.convert("RGB")
            else:
                pil_img = raw_img.convert("RGB")
        except Exception as e:
            raise VTONException(
                code=ErrorCode.CORRUPTED_IMAGE,
                message=f"Failed to decode {image_type} image: {str(e)}"
            )

        width, height = pil_img.size
        if width < cls.MIN_RESOLUTION or height < cls.MIN_RESOLUTION:
            raise VTONException(
                code=ErrorCode.IMAGE_TOO_SMALL,
                message=f"The {image_type} image dimensions ({width}x{height}) are smaller than minimum allowed ({cls.MIN_RESOLUTION}x{cls.MIN_RESOLUTION})."
            )

        if width > cls.MAX_RESOLUTION or height > cls.MAX_RESOLUTION:
            raise VTONException(
                code=ErrorCode.IMAGE_TOO_LARGE,
                message=f"The {image_type} image dimensions ({width}x{height}) exceed maximum allowed ({cls.MAX_RESOLUTION}x{cls.MAX_RESOLUTION})."
            )

        cv_img = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)

        # Blur analysis
        gray = cv2.cvtColor(cv_img, cv2.COLOR_BGR2GRAY)
        blur_score = cv2.Laplacian(gray, cv2.CV_64F).var()
        if blur_score < cls.MIN_LAPLACIAN_VAR:
            logger.warning(f"{image_type} image blur score low: {blur_score:.2f}")

        # Brightness analysis
        mean_brightness = np.mean(gray)
        if mean_brightness < 10.0 or mean_brightness > 245.0:
            logger.warning(f"{image_type} image mean brightness extreme: {mean_brightness:.2f}")

        return pil_img, cv_img

    @classmethod
    def validate_person_image(cls, pil_img: Image.Image, cv_img: np.ndarray) -> None:
        width, height = pil_img.size
        gray = cv2.cvtColor(cv_img, cv2.COLOR_BGR2GRAY)
        
        # Simple body presence & non-empty area check
        non_zero_ratio = np.count_nonzero(gray > 15) / (width * height)
        if non_zero_ratio < 0.20:
            raise VTONException(
                code=ErrorCode.INVALID_PERSON_IMAGE,
                message="The person image does not contain enough visible body area for virtual try-on."
            )

    @classmethod
    def validate_garment_image(cls, pil_img: Image.Image, cv_img: np.ndarray) -> None:
        width, height = pil_img.size
        gray = cv2.cvtColor(cv_img, cv2.COLOR_BGR2GRAY)
        
        non_zero_ratio = np.count_nonzero(gray > 10) / (width * height)
        if non_zero_ratio < 0.05:
            raise VTONException(
                code=ErrorCode.INVALID_GARMENT_IMAGE,
                message="The garment image does not contain a clearly visible garment item."
            )
