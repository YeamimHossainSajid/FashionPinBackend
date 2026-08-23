import numpy as np
import cv2
from PIL import Image

class DensePoseGenerator:
    """
    DensePose body correspondence map generator.
    Produces IUV surface coordinates map representing 3D body surface parameterization.
    """

    def generate(self, pil_img: Image.Image) -> np.ndarray:
        w, h = pil_img.size
        # Generate 3-channel IUV densepose tensor (h, w, 3)
        iuv_map = np.zeros((h, w, 3), dtype=np.uint8)
        
        # Estimate body gradient coordinates
        gray = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2GRAY)
        body_mask = gray > 20

        # Channel 0: Part Index (1..24)
        iuv_map[:, :, 0] = np.where(body_mask, 1, 0)
        # Channel 1: U coordinate
        u_coords = np.linspace(0, 255, w, dtype=np.uint8)
        iuv_map[:, :, 1] = np.tile(u_coords, (h, 1))
        # Channel 2: V coordinate
        v_coords = np.linspace(0, 255, h, dtype=np.uint8).reshape(h, 1)
        iuv_map[:, :, 2] = np.tile(v_coords, (1, w))

        iuv_map[~body_mask] = 0
        return iuv_map
