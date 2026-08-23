from fastapi import APIRouter, HTTPException, status
from app.vton.manager import ModelManager

router = APIRouter(tags=["Health"])

@router.get("/health", status_code=status.HTTP_200_OK)
def health_check():
    return {"status": "UP", "service": "virtual-tryon-service"}

@router.get("/ready")
def readiness_check():
    manager = ModelManager.get_instance()
    health_info = manager.health()
    
    if not health_info["ready"]:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail={"status": "DOWN", "reason": "VTON engine not loaded or GPU unavailable", "info": health_info}
        )
    return {"status": "UP", "details": health_info}

@router.get("/metrics")
def metrics():
    manager = ModelManager.get_instance()
    return manager.health()
