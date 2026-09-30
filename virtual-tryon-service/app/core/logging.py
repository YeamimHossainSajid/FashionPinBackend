import contextvars
import json
import logging
import sys
import time
from typing import Any, Dict, Optional

correlation_id_var: contextvars.ContextVar[Optional[str]] = contextvars.ContextVar("correlation_id", default=None)

def get_correlation_id() -> Optional[str]:
    return correlation_id_var.get()

def set_correlation_id(cid: str) -> None:
    correlation_id_var.set(cid)

class JSONFormatter(logging.Formatter):
    def format(self, record: logging.LogRecord) -> str:
        cid = get_correlation_id()
        log_obj: Dict[str, Any] = {
            "timestamp": self.formatTime(record, self.datefmt),
            "level": record.levelname,
            "logger": record.name,
            "message": record.getMessage(),
            "service": "virtual-tryon-service",
            "correlation_id": getattr(record, "correlation_id", cid),
        }
        if hasattr(record, "request_id"):
            log_obj["request_id"] = record.request_id
        elif cid:
            log_obj["request_id"] = cid
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
