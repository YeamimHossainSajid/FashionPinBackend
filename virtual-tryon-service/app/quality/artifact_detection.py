import numpy as np
import cv2
from PIL import Image

class ArtifactDetector:
    """
    Visual artifact & structural anomaly detector.
    """

    @classmethod
    def evaluate_artifacts(cls, final_img: Image.Image, original_img: Image.Image) -> float:
        final_np = np.array(final_img.convert("RGB"))
        orig_np = np.array(original_img.convert("RGB"))

        if final_np.shape != orig_np.shape:
            orig_np = cv2.resize(orig_np, (final_np.shape[1], final_np.shape[0]))

        # NaN / Inf pixel check
        if np.isnan(final_np).any() or np.isinf(final_np).any():
            return 0.0

        # Structural Mean Absolute Error outside boundary
        diff = np.abs(final_np.astype(np.float32) - orig_np.astype(np.float32))
        mae = np.mean(diff) / 255.0

        # Gradient variance check for unnatural high-frequency noise
        gray = cv2.cvtColor(final_np, cv2.COLOR_RGB2GRAY)
        laplacian_var = cv2.Laplacian(gray, cv2.CV_64F).var()

        if laplacian_var > 5000.0:
            return 0.50

        score = max(0.0, min(1.0, 1.0 - (mae * 0.5)))
        return round(float(score), 4)
