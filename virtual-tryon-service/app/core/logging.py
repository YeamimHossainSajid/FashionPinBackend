import json
import logging
import sys
import time
from typing import Any, Dict

class JSONFormatter(logging.Formatter):
    def format(self, record: logging.LogRecord) -> str:
        log_obj: Dict[str, Any] = {
            "timestamp": self.formatTime(record, self.datefmt),
            "level": record.levelname,
            "logger": record.name,
            "message": record.getMessage(),
            "service": "virtual-tryon-service",
        }
        if hasattr(record, "request_id"):
            log_obj["request_id"] = record.request_id
        if hasattr(record, "job_id"):
            log_obj["job_id"] = record.job_id

        if record.exc_info:
            log_obj["exception"] = self.formatException(record.exc_info)
        return json.dumps(log_obj)

def setup_logging(level: str = "INFO") -> logging.Logger:
    logger = logging.getLogger("vton_service")
    logger.setLevel(getattr(logging, level.upper(), logging.INFO))
    
    if not logger.handlers:
        handler = logging.StreamHandler(sys.stdout)
        handler.setFormatter(JSONFormatter())
        logger.addHandler(handler)
        
    return logger

logger = setup_logging()
