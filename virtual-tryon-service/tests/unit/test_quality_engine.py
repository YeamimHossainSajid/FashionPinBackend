from PIL import Image
import numpy as np
from app.quality.scorer import QualityEngine

def test_quality_engine_evaluation():
    engine = QualityEngine()
    person = Image.new("RGB", (200, 200), color="blue")
    garment = Image.new("RGB", (200, 200), color="blue")
    final = Image.new("RGB", (200, 200), color="blue")
    mask = np.zeros((200, 200), dtype=np.uint8)

    scores = engine.evaluate(final, person, garment, mask)

    assert scores.overall_score > 0.80
    assert scores.identity_score > 0.80
    assert engine.passes_gate(scores)
