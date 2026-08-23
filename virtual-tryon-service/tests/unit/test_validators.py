import pytest
from PIL import Image
import numpy as np
from app.preprocessing.input_validator import InputValidator
from app.core.exceptions import VTONException, ErrorCode

def test_validate_image_bytes_empty():
    with pytest.raises(VTONException) as exc_info:
        InputValidator.validate_image_bytes(b"", "person")
    assert exc_info.value.code == ErrorCode.CORRUPTED_IMAGE

def test_validate_image_bytes_too_small():
    small_img = Image.new("RGB", (100, 100), color="white")
    import io
    buf = io.BytesIO()
    small_img.save(buf, format="PNG")
    
    with pytest.raises(VTONException) as exc_info:
        InputValidator.validate_image_bytes(buf.getvalue(), "person")
    assert exc_info.value.code == ErrorCode.IMAGE_TOO_SMALL

def test_validate_image_bytes_valid():
    valid_img = Image.new("RGB", (512, 512), color="red")
    import io
    buf = io.BytesIO()
    valid_img.save(buf, format="PNG")

    pil_res, cv_res = InputValidator.validate_image_bytes(buf.getvalue(), "person")
    assert pil_res.size == (512, 512)
    assert cv_res.shape == (512, 512, 3)
