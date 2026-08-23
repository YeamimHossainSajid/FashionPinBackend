from enum import Enum
from typing import Any, Dict, Optional

class ErrorCode(str, Enum):
    INVALID_PERSON_IMAGE = "INVALID_PERSON_IMAGE"
    INVALID_GARMENT_IMAGE = "INVALID_GARMENT_IMAGE"
    CORRUPTED_IMAGE = "CORRUPTED_IMAGE"
    IMAGE_TOO_SMALL = "IMAGE_TOO_SMALL"
    IMAGE_TOO_LARGE = "IMAGE_TOO_LARGE"
    UNSUPPORTED_GARMENT_CATEGORY = "UNSUPPORTED_GARMENT_CATEGORY"
    PERSON_NOT_DETECTED = "PERSON_NOT_DETECTED"
    PREPROCESSING_FAILED = "PREPROCESSING_FAILED"
    VTON_GENERATION_FAILED = "VTON_GENERATION_FAILED"
    QUALITY_CHECK_FAILED = "QUALITY_CHECK_FAILED"
    JOB_NOT_FOUND = "JOB_NOT_FOUND"
    STORAGE_ERROR = "STORAGE_ERROR"
    INTERNAL_ERROR = "INTERNAL_ERROR"

class VTONException(Exception):
    def __init__(self, code: ErrorCode, message: str, details: Optional[Dict[str, Any]] = None):
        super().__init__(message)
        self.code = code
        self.message = message
        self.details = details or {}
