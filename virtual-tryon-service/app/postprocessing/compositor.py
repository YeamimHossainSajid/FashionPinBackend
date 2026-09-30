import numpy as np
import cv2
from PIL import Image
from app.postprocessing.protected_regions import ProtectedRegionsProcessor

class Compositor:
    """
    Deterministic alpha-feathered compositor:
    FINAL = ORIGINAL_PROTECTED_REGIONS + GENERATED_CLOTHING_REGION
    """

    @classmethod
    def reinhard_color_transfer(cls, source_rgb: np.ndarray, target_rgb: np.ndarray, blend_ratio: float = 0.35) -> np.ndarray:
        """
        Harmonizes ambient lighting between original person photo (source) and generated clothing (target)
        using Reinhard's color transfer in perceptual CIE L*a*b* space.
        """
        src_lab = cv2.cvtColor(source_rgb, cv2.COLOR_RGB2LAB).astype(np.float32)
        tgt_lab = cv2.cvtColor(target_rgb, cv2.COLOR_RGB2LAB).astype(np.float32)

        src_mean = np.mean(src_lab, axis=(0, 1))
        src_std = np.std(src_lab, axis=(0, 1)) + 1e-6

        tgt_mean = np.mean(tgt_lab, axis=(0, 1))
        tgt_std = np.std(tgt_lab, axis=(0, 1)) + 1e-6

        # Standardize and re-scale target to match source distribution
        harmonized_lab = ((tgt_lab - tgt_mean) * (src_std / tgt_std)) + src_mean
        harmonized_lab = np.clip(harmonized_lab, 0, 255).astype(np.uint8)
        harmonized_rgb = cv2.cvtColor(harmonized_lab, cv2.COLOR_LAB2RGB)

        # Blend subtle ratio to avoid over-saturating pure fabric dyes
        return cv2.addWeighted(target_rgb, 1.0 - blend_ratio, harmonized_rgb, blend_ratio, 0)

    @classmethod
    def composite(
        cls,
        original_person_img: Image.Image,
        generated_img: Image.Image,
        protected_mask: np.ndarray,
        harmonize_lighting: bool = True
    ) -> Image.Image:
        orig_np = np.array(original_person_img.convert("RGB"))
        gen_np = np.array(generated_img.convert("RGB"))

        h, w, c = orig_np.shape
        if gen_np.shape[:2] != (h, w):
            gen_np = cv2.resize(gen_np, (w, h), interpolation=cv2.INTER_LANCZOS4)

        if harmonize_lighting:
            gen_np = cls.reinhard_color_transfer(orig_np, gen_np, blend_ratio=0.25)

        if protected_mask is not None and np.any(protected_mask > 0):
            _, soft_mask = ProtectedRegionsProcessor.extract_protected_regions(
                original_person_img, protected_mask
            )
            # Seamless blend: original protected areas overlay generated clothing
            final_np = (orig_np * soft_mask + gen_np * (1.0 - soft_mask)).astype(np.uint8)
        else:
            final_np = gen_np

        return Image.fromarray(final_np, mode="RGB")
