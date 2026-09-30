import numpy as np
from PIL import Image
from typing import Dict, Any
from app.api.schemas.virtual_try_on import QualityScoreResponse
from app.quality.artifact_detection import ArtifactDetector
from app.quality.thresholds import QualityThresholds

class QualityEngine:
    """
    Evaluates VTON generation outputs on identity preservation, garment fidelity,
    structural placement, and artifact presence.
    """

    def __init__(self, thresholds: QualityThresholds = QualityThresholds()):
        self.thresholds = thresholds

    def evaluate(
        self,
        final_img: Image.Image,
        original_person_img: Image.Image,
        garment_img: Image.Image,
        protected_mask: np.ndarray
    ) -> QualityScoreResponse:
        # Identity Score: Similarity in face/protected region
        identity_score = self._compute_identity_score(final_img, original_person_img, protected_mask)

        # Garment Score: Color & texture histogram correlation
        garment_score = self._compute_garment_score(final_img, garment_img)

        # Structural Score: Edge alignment & pose preservation
        structure_score = self._compute_structure_score(final_img, original_person_img)

        # Artifact Score: Distortion and noise detection
        artifact_score = ArtifactDetector.evaluate_artifacts(final_img, original_person_img)

        # Overall Weighted Score
        overall_score = round(
            (0.35 * identity_score) +
            (0.25 * garment_score) +
            (0.20 * structure_score) +
            (0.20 * artifact_score),
            4
        )

        return QualityScoreResponse(
            overall_score=overall_score,
            identity_score=identity_score,
            garment_score=garment_score,
            structure_score=structure_score,
            artifact_score=artifact_score
        )

    def passes_gate(self, scores: QualityScoreResponse) -> bool:
        return (
            scores.overall_score >= self.thresholds.overall_threshold and
            scores.identity_score >= self.thresholds.identity_threshold and
            scores.garment_score >= self.thresholds.garment_threshold and
            scores.artifact_score >= self.thresholds.artifact_threshold
        )

    def _compute_identity_score(
        self,
        final_img: Image.Image,
        orig_img: Image.Image,
        protected_mask: np.ndarray
    ) -> float:
        if protected_mask is None or not np.any(protected_mask > 0):
            return 0.95

        f_np = np.array(final_img.convert("RGB"))
        o_np = np.array(orig_img.convert("RGB"))

        mask_bool = protected_mask > 0
        diff = np.abs(f_np[mask_bool].astype(np.float32) - o_np[mask_bool].astype(np.float32))
        mae = np.mean(diff) / 255.0 if diff.size > 0 else 0.0

        return round(max(0.0, min(1.0, 1.0 - mae)), 4)

    def _compute_garment_score(self, final_img: Image.Image, garment_img: Image.Image) -> float:
        f_hist = np.histogram(np.array(final_img.convert("RGB")), bins=16, range=(0, 256))[0]
        g_hist = np.histogram(np.array(garment_img.convert("RGB")), bins=16, range=(0, 256))[0]

        f_hist = f_hist / (np.sum(f_hist) + 1e-7)
        g_hist = g_hist / (np.sum(g_hist) + 1e-7)

        sim = np.sum(np.minimum(f_hist, g_hist))
        return round(float(sim), 4)

    def _compute_structure_score(self, final_img: Image.Image, orig_img: Image.Image) -> float:
        import cv2
        f_np = np.array(final_img.convert("RGB"))
        o_np = np.array(orig_img.convert("RGB"))

        if f_np.shape != o_np.shape:
            o_np = cv2.resize(o_np, (f_np.shape[1], f_np.shape[0]))

        f_gray = cv2.cvtColor(f_np, cv2.COLOR_RGB2GRAY)
        o_gray = cv2.cvtColor(o_np, cv2.COLOR_RGB2GRAY)

        # Sobel edge gradient magnitude
        f_edge_x = cv2.Sobel(f_gray, cv2.CV_32F, 1, 0, ksize=3)
        f_edge_y = cv2.Sobel(f_gray, cv2.CV_32F, 0, 1, ksize=3)
        f_mag = cv2.magnitude(f_edge_x, f_edge_y)

        o_edge_x = cv2.Sobel(o_gray, cv2.CV_32F, 1, 0, ksize=3)
        o_edge_y = cv2.Sobel(o_gray, cv2.CV_32F, 0, 1, ksize=3)
        o_mag = cv2.magnitude(o_edge_x, o_edge_y)

        # Normalized cross correlation of gradient magnitudes
        f_norm = f_mag - np.mean(f_mag)
        o_norm = o_mag - np.mean(o_mag)
        denominator = (np.linalg.norm(f_norm) * np.linalg.norm(o_norm)) + 1e-7
        corr = float(np.sum(f_norm * o_norm) / denominator)

        # Bound to [0.0, 1.0]
        structure_score = max(0.0, min(1.0, (corr + 1.0) / 2.0))
        return round(float(structure_score), 4)
