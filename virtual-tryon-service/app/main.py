from contextlib import asynccontextmanager
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import settings
from app.core.logging import logger
from app.core.exceptions import VTONException
from app.vton.manager import ModelManager
from app.messaging.kafka import kafka_client
from app.api.routes import health, virtual_try_on
# Ensure engine registration imports
import app.vton.engines

@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info(f"Starting {settings.service_name} on port {settings.service_port}...")
    
    # Pre-load VTON Model & GPU Allocator during worker startup
    try:
        manager = ModelManager.get_instance()
        manager.load()
    except Exception as e:
        logger.error(f"Failed to pre-load model at startup: {str(e)}")

    # Start Kafka Background Consumer
    kafka_client.start_consumer()
    
    yield
    
    logger.info(f"Shutting down {settings.service_name}...")
    kafka_client.stop_consumer()
    ModelManager.get_instance().unload()

app = FastAPI(
    title="Fashion Pin - Virtual Try-On AI Service",
    description="Production-grade, model-agnostic, GPU-optimized Virtual Try-On microservice.",
    version=settings.model_version,
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.exception_handler(VTONException)
async def vton_exception_handler(request: Request, exc: VTONException):
    return JSONResponse(
        status_code=400,
        content={"code": exc.code, "message": exc.message, "details": exc.details}
    )

app.include_router(health.router)
app.include_router(virtual_try_on.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=settings.service_port, reload=settings.debug)
