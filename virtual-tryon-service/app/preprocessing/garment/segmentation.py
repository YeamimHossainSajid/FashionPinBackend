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
        # Ensure RGB base
        if pil_img.mode == "RGBA":
            img_rgba = np.array(pil_img)
            existing_alpha = img_rgba[:, :, 3]
            img_rgb = img_rgba[:, :, :3]
            # If the alpha channel already isolates the garment, use it directly
            if 0 < np.count_nonzero(existing_alpha < 250) < (existing_alpha.size * 0.95):
                alpha = existing_alpha
            else:
                alpha = None
        else:
            pil_rgb = pil_img.convert("RGB")
            img_rgb = np.array(pil_rgb)
            alpha = None

        h, w, _ = img_rgb.shape

        if alpha is None:
            gray = cv2.cvtColor(img_rgb, cv2.COLOR_RGB2GRAY)
            # Threshold background (check corners for background illumination)
            corner_brightness = np.mean([gray[0, 0], gray[0, w - 1], gray[h - 1, 0], gray[h - 1, w - 1]])
            if corner_brightness > 220:
                _, alpha = cv2.threshold(gray, 240, 255, cv2.THRESH_BINARY_INV)
            elif corner_brightness < 35:
                _, alpha = cv2.threshold(gray, 30, 255, cv2.THRESH_BINARY)
            else:
                # Use Otsu's thresholding for ambiguous lighting
                _, alpha = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

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
            
            cropped_rgb = img_rgb[y:y+crop_h, x:x+crop_w]
            cropped_alpha = alpha[y:y+crop_h, x:x+crop_w]
        else:
            cropped_rgb = img_rgb
            cropped_alpha = alpha

        # Combine RGB (3 channels) + Alpha (1 channel) = RGBA (4 channels)
        rgba_np = np.dstack((cropped_rgb[:, :, :3], cropped_alpha))
        cropped_garment = Image.fromarray(rgba_np, mode="RGBA")
        garment_mask = Image.fromarray(cropped_alpha, mode="L")

        return cropped_garment, garment_mask
