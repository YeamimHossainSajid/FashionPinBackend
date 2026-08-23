from typing import Tuple
import numpy as np
import cv2
from PIL import Image

class ProtectedRegionsProcessor:
    """
    Extracts and isolates original protected regions (face, hair, hands, accessories).
    """

    @classmethod
    def extract_protected_regions(
        cls,
        original_person_img: Image.Image,
        protected_mask: np.ndarray
    ) -> Tuple[np.ndarray, np.ndarray]:
        """
        Returns (protected_rgb_array, soft_alpha_mask)
        """
        person_np = np.array(original_person_img.convert("RGB"))
        
        # Feather mask edges with Gaussian blur to prevent sharp seams
        soft_mask = cv2.GaussianBlur(protected_mask.astype(np.float32), (9, 9), 0) / 255.0
        soft_mask = np.clip(soft_mask, 0.0, 1.0)[:, :, np.newaxis]

        protected_rgb = (person_np * soft_mask).astype(np.uint8)
        return protected_rgb, soft_mask
