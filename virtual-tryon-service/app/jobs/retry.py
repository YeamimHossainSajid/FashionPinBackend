from dataclasses import dataclass
from typing import List, Optional
from PIL import Image
from app.api.schemas.virtual_try_on import TryOnConfig, QualityScoreResponse
from app.core.config import settings
from app.core.logging import logger

@dataclass
class AttemptResult:
    attempt_number: int
    seed: int
    image: Image.Image
    scores: QualityScoreResponse
    config: TryOnConfig

class RetryManager:
    """
    Manages deterministic VTON retry attempts and optimal result selection.
    Max attempts = 2.
    """

    def __init__(self, max_attempts: int = 2):
        self.max_attempts = max_attempts

    def prepare_retry_config(self, attempt_number: int, initial_config: TryOnConfig) -> TryOnConfig:
        if attempt_number == 1:
            return initial_config

        # Perturb seed and increase guidance scale for attempt 2
        retry_config = initial_config.model_copy()
        retry_config.seed = (initial_config.seed or 42) + 100
        retry_config.guidance_scale = min(10.0, initial_config.guidance_scale + 0.5)
        retry_config.num_inference_steps = min(100, initial_config.num_inference_steps + 10)
        
        logger.info(f"Prepared retry attempt {attempt_number} config: seed={retry_config.seed}, guidance={retry_config.guidance_scale}")
        return retry_config

    def select_best_attempt(self, attempts: List[AttemptResult]) -> AttemptResult:
        if not attempts:
            raise ValueError("No attempt results provided to select_best_attempt.")

        if len(attempts) == 1:
            return attempts[0]

        # Select attempt with higher overall quality score
        best = max(attempts, key=lambda a: a.scores.overall_score)
        logger.info(f"Selected Attempt {best.attempt_number} with highest score: {best.scores.overall_score:.4f}")
        return best
