import numpy as np
import cv2
from PIL import Image
from typing import Tuple

class GarmentSegmenter:
    """
    Garment background removal & bounding box cropping.
    """

    @classmethod
    def segment_and_crop(cls, pil_img: Image.Image) -> Tuple[Image.Image, Image.Image]:
        """
        Returns (cropped_rgba_garment, garment_binary_mask)
        """
        img_np = np.array(pil_img)
        h, w, _ = img_np.shape

        gray = cv2.cvtColor(img_np, cv2.COLOR_RGB2GRAY)
        
        # Threshold background (assume white or near-white background if corners are light)
        corner_brightness = np.mean([gray[0,0], gray[0, w-1], gray[h-1, 0], gray[h-1, w-1]])
        if corner_brightness > 200:
            _, alpha = cv2.threshold(gray, 240, 255, cv2.THRESH_BINARY_INV)
        else:
            _, alpha = cv2.threshold(gray, 20, 255, cv2.THRESH_BINARY)

        # Morphological cleanup
        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
        alpha = cv2.morphologyEx(alpha, cv2.MORPH_CLOSE, kernel)

        # Bounding box crop
        coords = cv2.findNonZero(alpha)
        if coords is not None:
            x, y, crop_w, crop_h = cv2.boundingRect(coords)
            # Add padding
            pad = 10
            x = max(0, x - pad)
            y = max(0, y - pad)
            crop_w = min(w - x, crop_w + 2 * pad)
            crop_h = min(h - y, crop_h + 2 * pad)
            
            cropped_np = img_np[y:y+crop_h, x:x+crop_w]
            cropped_alpha = alpha[y:y+crop_h, x:x+crop_w]
        else:
            cropped_np = img_np
            cropped_alpha = alpha

        # Combine RGB + Alpha
        rgba_np = np.dstack((cropped_np, cropped_alpha))
        cropped_garment = Image.fromarray(rgba_np, mode="RGBA")
        garment_mask = Image.fromarray(cropped_alpha, mode="L")

        return cropped_garment, garment_mask
