import numpy as np
import cv2
from PIL import Image

class ArtifactDetector:
    """
    Visual artifact & structural anomaly detector using SSIM and high-frequency edge analysis.
    """

    @classmethod
    def compute_ssim(cls, img1: np.ndarray, img2: np.ndarray) -> float:
        """
        Computes Structural Similarity Index (SSIM) between two grayscale images.
        """
        c1 = (0.01 * 255) ** 2
        c2 = (0.03 * 255) ** 2

        img1 = img1.astype(np.float64)
        img2 = img2.astype(np.float64)

        kernel = cv2.getGaussianKernel(11, 1.5)
        window = np.outer(kernel, kernel.transpose())

        mu1 = cv2.filter2D(img1, -1, window)[5:-5, 5:-5]
        mu2 = cv2.filter2D(img2, -1, window)[5:-5, 5:-5]

        mu1_sq = mu1 ** 2
        mu2_sq = mu2 ** 2
        mu1_mu2 = mu1 * mu2

        sigma1_sq = cv2.filter2D(img1 ** 2, -1, window)[5:-5, 5:-5] - mu1_sq
        sigma2_sq = cv2.filter2D(img2 ** 2, -1, window)[5:-5, 5:-5] - mu2_sq
        sigma12 = cv2.filter2D(img1 * img2, -1, window)[5:-5, 5:-5] - mu1_mu2

        ssim_map = ((2 * mu1_mu2 + c1) * (2 * sigma12 + c2)) / ((mu1_sq + mu2_sq + c1) * (sigma1_sq + sigma2_sq + c2))
        return float(np.clip(ssim_map.mean(), 0.0, 1.0))

    @classmethod
    def evaluate_artifacts(cls, final_img: Image.Image, original_img: Image.Image) -> float:
        final_np = np.array(final_img.convert("RGB"))
        orig_np = np.array(original_img.convert("RGB"))

        if final_np.shape != orig_np.shape:
            orig_np = cv2.resize(orig_np, (final_np.shape[1], final_np.shape[0]))

        # NaN / Inf pixel check
        if np.isnan(final_np).any() or np.isinf(final_np).any():
            return 0.0

        # Grayscale conversion for structural analysis
        gray_final = cv2.cvtColor(final_np, cv2.COLOR_RGB2GRAY)
        gray_orig = cv2.cvtColor(orig_np, cv2.COLOR_RGB2GRAY)

        # High-frequency noise / unnatural blur check using Laplacian
        laplacian_var = float(cv2.Laplacian(gray_final, cv2.CV_64F).var())
        if laplacian_var > 6000.0 or laplacian_var < 5.0:
            # Extreme noise or total blur anomaly
            penalty = 0.4
        else:
            penalty = 0.0

        # SSIM calculation
        ssim_val = cls.compute_ssim(gray_final, gray_orig)

        # Combine SSIM with noise penalty
        score = max(0.0, min(1.0, ssim_val - penalty))
        return round(float(score), 4)
