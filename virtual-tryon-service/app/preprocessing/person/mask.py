import numpy as np
import cv2
from PIL import Image
from app.api.schemas.virtual_try_on import GarmentCategory

class MaskGenerator:
    """
    Generates clothing-agnostic masks and protected-region masks from human parsing map.
    """

    @classmethod
    def generate_agnostic_mask(cls, parse_map: np.ndarray, category: GarmentCategory) -> np.ndarray:
        h, w = parse_map.shape
        mask = np.zeros((h, w), dtype=np.uint8)

        if category == GarmentCategory.UPPER_BODY:
            # Mask upper-clothes (5), dress (6), coat (7), torso-skin (10)
            target_labels = [5, 6, 7, 10]
        elif category == GarmentCategory.LOWER_BODY:
            # Mask pants (9), skirt (12), socks (8)
            target_labels = [8, 9, 12]
        elif category in (GarmentCategory.DRESS, GarmentCategory.FULL_BODY):
            # Mask upper-clothes (5), dress (6), coat (7), pants (9), torso-skin (10), skirt (12)
            target_labels = [5, 6, 7, 8, 9, 10, 12]
        else:
            target_labels = [5, 6, 7, 10]

        for label in target_labels:
            mask[parse_map == label] = 255

        # Morphological dilation to ensure complete coverage around seams
        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (15, 15))
        mask = cv2.dilate(mask, kernel, iterations=1)
        return mask

    @classmethod
    def generate_protected_mask(
        cls,
        parse_map: np.ndarray,
        preserve_face: bool = True,
        preserve_hair: bool = True,
        preserve_hands: bool = True
    ) -> np.ndarray:
        h, w = parse_map.shape
        protected_mask = np.zeros((h, w), dtype=np.uint8)

        # Face (13), Hat (1), Sunglasses (4)
        if preserve_face:
            protected_mask[np.isin(parse_map, [1, 4, 13])] = 255

        # Hair (2)
        if preserve_hair:
            protected_mask[parse_map == 2] = 255

        # Left-arm (14), Right-arm (15), Glove (3)
        if preserve_hands:
            protected_mask[np.isin(parse_map, [3, 14, 15])] = 255

        return protected_mask
