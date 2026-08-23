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
    def composite(
        cls,
        original_person_img: Image.Image,
        generated_img: Image.Image,
        protected_mask: np.ndarray
    ) -> Image.Image:
        orig_np = np.array(original_person_img.convert("RGB"))
        gen_np = np.array(generated_img.convert("RGB"))

        h, w, c = orig_np.shape
        if gen_np.shape[:2] != (h, w):
            gen_np = cv2.resize(gen_np, (w, h), interpolation=cv2.INTER_LANCZOS4)

        if protected_mask is not None and np.any(protected_mask > 0):
            _, soft_mask = ProtectedRegionsProcessor.extract_protected_regions(
                original_person_img, protected_mask
            )
            # Seamless blend: original protected areas overlay generated clothing
            final_np = (orig_np * soft_mask + gen_np * (1.0 - soft_mask)).astype(np.uint8)
        else:
            final_np = gen_np

        return Image.fromarray(final_np, mode="RGB")
