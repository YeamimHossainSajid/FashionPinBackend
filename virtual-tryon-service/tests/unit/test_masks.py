import numpy as np
from app.preprocessing.person.mask import MaskGenerator
from app.api.schemas.virtual_try_on import GarmentCategory

def test_generate_agnostic_mask():
    parse_map = np.zeros((100, 100), dtype=np.uint8)
    parse_map[20:60, 20:80] = 5  # Upper clothes label

    mask = MaskGenerator.generate_agnostic_mask(parse_map, GarmentCategory.UPPER_BODY)
    assert mask.shape == (100, 100)
    assert np.any(mask > 0)

def test_generate_protected_mask():
    parse_map = np.zeros((100, 100), dtype=np.uint8)
    parse_map[10:30, 40:60] = 13  # Face label
    parse_map[0:10, 40:60] = 2    # Hair label

    prot_mask = MaskGenerator.generate_protected_mask(parse_map, preserve_face=True, preserve_hair=True)
    assert prot_mask.shape == (100, 100)
    assert np.any(prot_mask > 0)
