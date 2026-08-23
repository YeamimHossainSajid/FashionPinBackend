from dataclasses import dataclass
import time
import numpy as np
import cv2
from PIL import Image

@dataclass
class BenchmarkResult:
    engine_name: str
    model_version: str
    sample_id: str
    mae_score: float
    ssim_score: float
    garment_fidelity: float
    latency_ms: float
    vram_mb: float

class BenchmarkMetrics:
    @classmethod
    def compute_ssim(cls, img1: Image.Image, img2: Image.Image) -> float:
        arr1 = np.array(img1.convert("L"), dtype=np.float32)
        arr2 = np.array(img2.convert("L"), dtype=np.float32)

        if arr1.shape != arr2.shape:
            arr2 = cv2.resize(arr2, (arr1.shape[1], arr1.shape[0]))

        C1 = (0.01 * 255) ** 2
        C2 = (0.03 * 255) ** 2

        mu1 = cv2.GaussianBlur(arr1, (11, 11), 1.5)
        mu2 = cv2.GaussianBlur(arr2, (11, 11), 1.5)

        mu1_sq = mu1 ** 2
        mu2_sq = mu2 ** 2
        mu1_mu2 = mu1 * mu2

        sigma1_sq = cv2.GaussianBlur(arr1 ** 2, (11, 11), 1.5) - mu1_sq
        sigma2_sq = cv2.GaussianBlur(arr2 ** 2, (11, 11), 1.5) - mu2_sq
        sigma12 = cv2.GaussianBlur(arr1 * arr2, (11, 11), 1.5) - mu1_mu2

        ssim_map = ((2 * mu1_mu2 + C1) * (2 * sigma12 + C2)) / ((mu1_sq + mu2_sq + C1) * (sigma1_sq + sigma2_sq + C2))
        return round(float(np.mean(ssim_map)), 4)

    @classmethod
    def compute_mae(cls, img1: Image.Image, img2: Image.Image) -> float:
        arr1 = np.array(img1.convert("RGB"), dtype=np.float32)
        arr2 = np.array(img2.convert("RGB"), dtype=np.float32)

        if arr1.shape != arr2.shape:
            arr2 = cv2.resize(arr2, (arr1.shape[1], arr1.shape[0]))

        mae = np.mean(np.abs(arr1 - arr2)) / 255.0
        return round(float(mae), 4)
