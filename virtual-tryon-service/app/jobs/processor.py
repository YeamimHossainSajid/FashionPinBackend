import datetime
import time
import uuid
from typing import Dict, Any, Optional
from PIL import Image

from app.api.schemas.virtual_try_on import (
    CreateVTONJobRequest, VTONJobResponse, JobStatus, QualityScoreResponse, TryOnConfig
)
from app.core.config import settings
from app.core.logging import logger
from app.core.exceptions import VTONException, ErrorCode
from app.preprocessing.input_validator import InputValidator
from app.preprocessing.person.parser import HumanParser
from app.preprocessing.person.densepose import DensePoseGenerator
from app.preprocessing.person.mask import MaskGenerator
from app.preprocessing.garment.processor import GarmentProcessor
from app.vton.manager import ModelManager
from app.vton.base import PersonData, GarmentData
from app.postprocessing.compositor import Compositor
from app.quality.scorer import QualityEngine
from app.jobs.retry import RetryManager, AttemptResult
from app.storage.object_storage import storage_client

# In-memory Job Repository Store (production uses Postgres / Redis)
JOB_STORE: Dict[str, VTONJobResponse] = {}

class JobProcessor:
    def __init__(self):
        self.model_manager = ModelManager.get_instance()
        self.human_parser = HumanParser()
        self.densepose_gen = DensePoseGenerator()
        self.quality_engine = QualityEngine()
        self.retry_manager = RetryManager(max_attempts=settings.max_attempts)

    def create_job(self, request: CreateVTONJobRequest) -> VTONJobResponse:
        job_id = f"vton_{uuid.uuid4().hex[:12]}"
        now_str = datetime.datetime.utcnow().isoformat() + "Z"

        job = VTONJobResponse(
            job_id=job_id,
            status=JobStatus.QUEUED,
            model_version=settings.model_version,
            pipeline_version=settings.pipeline_version,
            attempt_count=0,
            created_at=now_str,
            updated_at=now_str,
        )
        JOB_STORE[job_id] = job
        return job

    def execute_job(self, job_id: str, request: CreateVTONJobRequest) -> VTONJobResponse:
        start_time = time.time()
        job = JOB_STORE.get(job_id) or self.create_job(request)

        try:
            # 1. Validation Phase
            self._update_status(job, JobStatus.VALIDATING)
            person_img = storage_client.download_image(request.person_image_url)
            garment_img = storage_client.download_image(request.garment_image_url)

            person_pil, person_cv = InputValidator.validate_image_bytes(
                self._pil_to_bytes(person_img), "person"
            )
            InputValidator.validate_person_image(person_pil, person_cv)

            garment_pil, garment_cv = InputValidator.validate_image_bytes(
                self._pil_to_bytes(garment_img), "garment"
            )
            InputValidator.validate_garment_image(garment_pil, garment_cv)

            # 2. Preprocessing Phase
            self._update_status(job, JobStatus.PREPROCESSING)
            parse_map = self.human_parser.parse(person_pil)
            densepose_map = self.densepose_gen.generate(person_pil)
            agnostic_mask = MaskGenerator.generate_agnostic_mask(parse_map, request.garment_category)
            protected_mask = MaskGenerator.generate_protected_mask(
                parse_map,
                preserve_face=request.config.preserve_face if request.config else True,
                preserve_hair=request.config.preserve_hair if request.config else True,
                preserve_hands=request.config.preserve_hands if request.config else True,
            )

            processed_garment = GarmentProcessor.process(
                garment_pil,
                request.garment_category,
                (settings.image_width, settings.image_height)
            )

            person_data = PersonData(
                image=person_pil,
                parse_map=parse_map,
                densepose_map=densepose_map,
                agnostic_mask=agnostic_mask,
                protected_mask=protected_mask,
            )
            garment_data = GarmentData(
                image=processed_garment,
                category=request.garment_category
            )

            # 3. Generation & Quality Check Pipeline with Retry
            self._update_status(job, JobStatus.GENERATING)
            engine = self.model_manager.get_engine()
            initial_config = request.config or TryOnConfig()

            attempts = []
            for attempt_num in range(1, settings.max_attempts + 1):
                job.attempt_count = attempt_num
                current_config = self.retry_manager.prepare_retry_config(attempt_num, initial_config)

                if attempt_num > 1:
                    self._update_status(job, JobStatus.RETRYING)

                # VTON Inference
                vton_output = engine.generate(person_data, garment_data, current_config)

                # Compositing
                final_img = Compositor.composite(
                    person_pil, vton_output.generated_image, protected_mask
                )

                # Quality Scoring
                self._update_status(job, JobStatus.QUALITY_CHECK)
                scores = self.quality_engine.evaluate(
                    final_img, person_pil, processed_garment, protected_mask
                )

                attempt_res = AttemptResult(
                    attempt_number=attempt_num,
                    seed=current_config.seed or 42,
                    image=final_img,
                    scores=scores,
                    config=current_config
                )
                attempts.append(attempt_res)

                if self.quality_engine.passes_gate(scores):
                    logger.info(f"Job {job_id} attempt {attempt_num} PASSED quality gate (overall_score={scores.overall_score:.4f})")
                    break

            # 4. Result Selection & Upload
            best_attempt = self.retry_manager.select_best_attempt(attempts)
            result_url = storage_client.upload_image(best_attempt.image, filename_prefix=f"vton_{job_id}")

            job.status = JobStatus.COMPLETED
            job.result_url = result_url
            job.quality_score = best_attempt.scores.overall_score
            job.scores = best_attempt.scores
            job.updated_at = datetime.datetime.utcnow().isoformat() + "Z"

            logger.info(f"Job {job_id} COMPLETED in {time.time() - start_time:.2f}s with quality={job.quality_score:.4f}")
            JOB_STORE[job_id] = job
            return job

        except VTONException as ve:
            logger.error(f"Job {job_id} failed with VTONException ({ve.code}): {ve.message}")
            job.status = JobStatus.FAILED
            job.failure_reason = f"[{ve.code}] {ve.message}"
            job.updated_at = datetime.datetime.utcnow().isoformat() + "Z"
            JOB_STORE[job_id] = job
            return job
        except Exception as e:
            logger.error(f"Job {job_id} failed with unhandled error: {str(e)}", exc_info=True)
            job.status = JobStatus.FAILED
            job.failure_reason = f"[{ErrorCode.INTERNAL_ERROR}] {str(e)}"
            job.updated_at = datetime.datetime.utcnow().isoformat() + "Z"
            JOB_STORE[job_id] = job
            return job

    def get_job(self, job_id: str) -> Optional[VTONJobResponse]:
        return JOB_STORE.get(job_id)

    def _update_status(self, job: VTONJobResponse, status: JobStatus) -> None:
        job.status = status
        job.updated_at = datetime.datetime.utcnow().isoformat() + "Z"
        JOB_STORE[job.job_id] = job
        logger.info(f"Job {job.job_id} status changed -> {status}")

    def _pil_to_bytes(self, img: Image.Image) -> bytes:
        import io
        buf = io.BytesIO()
        img.save(buf, format="PNG")
        return buf.getvalue()

job_processor = JobProcessor()
