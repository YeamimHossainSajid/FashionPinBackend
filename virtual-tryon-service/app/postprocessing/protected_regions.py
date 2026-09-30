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
        
        # Normalize mask to [0.0, 1.0] regardless of input scale ([0, 1] boolean or [0, 255] uint8)
        mask_f = protected_mask.astype(np.float32)
        max_val = float(np.max(mask_f)) if mask_f.size > 0 else 0.0
        if max_val > 1.0:
            mask_f = mask_f / 255.0

        # Feather mask edges with Gaussian blur to prevent sharp seams
        soft_mask = cv2.GaussianBlur(mask_f, (9, 9), 0)
        soft_mask = np.clip(soft_mask, 0.0, 1.0)
        if soft_mask.ndim == 2:
            soft_mask = soft_mask[:, :, np.newaxis]

        protected_rgb = (person_np * soft_mask).astype(np.uint8)
        return protected_rgb, soft_mask
