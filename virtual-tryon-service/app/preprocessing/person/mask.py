import numpy as np
import cv2
from PIL import Image
from app.api.schemas.virtual_try_on import GarmentCategory

class MaskGenerator:
    """
    Generates clothing-agnostic masks and protected-region masks from human parsing map.
    """

    @classmethod
    def generate_agnostic_mask(
        cls,
        parse_map: np.ndarray,
        category: GarmentCategory,
        dilate_radius: int = 15,
        feather_radius: int = 0
    ) -> np.ndarray:
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

        # Morphological dilation to ensure complete coverage around clothing seams
        if dilate_radius > 0:
            k_size = dilate_radius if dilate_radius % 2 == 1 else dilate_radius + 1
            kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (k_size, k_size))
            mask = cv2.dilate(mask, kernel, iterations=1)

        # Smooth holes and jagged boundaries
        close_kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
        mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, close_kernel)

        if feather_radius > 0:
            f_size = feather_radius if feather_radius % 2 == 1 else feather_radius + 1
            mask = cv2.GaussianBlur(mask, (f_size, f_size), 0)

        return mask

    @classmethod
    def resolve_mask_conflicts(cls, agnostic_mask: np.ndarray, protected_mask: np.ndarray) -> np.ndarray:
        """
        Ensures protected regions (face, hair, hands) are never accidentally overwritten by agnostic inpainting mask.
        """
        clean_agnostic = agnostic_mask.copy()
        clean_agnostic[protected_mask > 127] = 0
        return clean_agnostic

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

        # Face (13), Hat (1), Sunglasses (4), Neck (11)
        if preserve_face:
            protected_mask[np.isin(parse_map, [1, 4, 11, 13])] = 255

        # Hair (2)
        if preserve_hair:
            protected_mask[parse_map == 2] = 255

        # Left-arm (14), Right-arm (15), Glove (3)
        if preserve_hands:
            protected_mask[np.isin(parse_map, [3, 14, 15])] = 255

        # Clean up mask boundaries
        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (3, 3))
        protected_mask = cv2.morphologyEx(protected_mask, cv2.MORPH_CLOSE, kernel)

        return protected_mask
