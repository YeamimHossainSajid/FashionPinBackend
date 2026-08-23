from fastapi import APIRouter, BackgroundTasks, HTTPException, status
from app.api.schemas.virtual_try_on import CreateVTONJobRequest, VTONJobResponse
from app.jobs.processor import job_processor
from app.core.exceptions import VTONException

router = APIRouter(tags=["Virtual Try-On"])

@router.post(
    "/api/v1/virtual-try-on",
    response_model=VTONJobResponse,
    status_code=status.HTTP_202_ACCEPTED,
    summary="Create Virtual Try-On Job"
)
def create_virtual_try_on(
    request: CreateVTONJobRequest,
    background_tasks: BackgroundTasks,
    sync: bool = False
):
    job = job_processor.create_job(request)
    
    if sync:
        # Synchronous execution mode for fast local verification/testing
        completed_job = job_processor.execute_job(job.job_id, request)
        if completed_job.status == "FAILED":
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail={"code": "VTON_FAILED", "message": completed_job.failure_reason, "job_id": job.job_id}
            )
        return completed_job
    else:
        # Asynchronous execution in background task
        background_tasks.add_task(job_processor.execute_job, job.job_id, request)
        return job

@router.get(
    "/api/v1/virtual-try-on/{job_id}",
    response_model=VTONJobResponse,
    status_code=status.HTTP_200_OK,
    summary="Get Virtual Try-On Job Status"
)
def get_virtual_try_on_job(job_id: str):
    job = job_processor.get_job(job_id)
    if not job:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={"code": "JOB_NOT_FOUND", "message": f"Try-on job {job_id} not found."}
        )
    return job
