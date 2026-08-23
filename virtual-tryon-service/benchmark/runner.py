import time
import os
import numpy as np
from PIL import Image
import torch
from benchmark.metrics import BenchmarkMetrics, BenchmarkResult
from benchmark.report import BenchmarkReport
from app.vton.manager import ModelManager
from app.vton.registry import VTONRegistry
from app.vton.base import PersonData, GarmentData
import app.vton.engines
from app.preprocessing.person.parser import HumanParser
from app.preprocessing.person.densepose import DensePoseGenerator
from app.preprocessing.person.mask import MaskGenerator
from app.preprocessing.garment.processor import GarmentProcessor
from app.preprocessing.garment.segmentation import GarmentSegmenter
from app.postprocessing.compositor import Compositor
from app.quality.scorer import QualityEngine

def run_benchmark():
    print("Initializing VTON Engine Benchmark Runner...")

    engine_name = "catvton" if VTONRegistry.get_engine_class("catvton") else "fashn-vton"
    try:
        engine_cls = VTONRegistry.get_engine_class(engine_name)
        engine = engine_cls()
        engine.initialize()
    except Exception as e:
        print(f"Note: Could not initialize engine '{engine_name}' ({e}). Running synthetic benchmark suite.")
        engine_cls = VTONRegistry.get_engine_class("catvton")
        engine = engine_cls()
        engine.initialize()

    # Generate synthetic benchmark sample images
    person_img = Image.new("RGB", (512, 512), color="white")
    garment_img = Image.new("RGB", (512, 512), color="blue")
    
    start_time = time.time()
    
    # Run pre-processing steps
    parser = HumanParser()
    mask_gen = MaskGenerator()
    parse_map = parser.parse(person_img)
    agnostic_mask = mask_gen.generate_agnostic_mask(parse_map, category="UPPER_BODY")
    protected_mask = mask_gen.generate_protected_mask(parse_map)
    
    garment_data = GarmentData(image=garment_img, category="UPPER_BODY", mask=None)
    person_data = PersonData(
        image=person_img,
        parse_map=parse_map,
        densepose_map=parse_map,
        agnostic_mask=agnostic_mask,
        protected_mask=protected_mask
    )
    
    vton_out = engine.generate(person_data, garment_data, config={"num_inference_steps": 30, "seed": 42})
    
    # Run post-processing compositing & quality evaluation
    compositor = Compositor()
    final_img = compositor.composite(vton_out.generated_image, person_img, agnostic_mask)
    
    quality_engine = QualityEngine()
    scores = quality_engine.evaluate(final_img, person_img, garment_img, agnostic_mask)
    
    latency_ms = (time.time() - start_time) * 1000.0
    
    ssim_val = BenchmarkMetrics.compute_ssim(final_img, person_img)
    mae_val = BenchmarkMetrics.compute_mae(final_img, person_img)
    
    vram_mb = 0.0
    if torch.cuda.is_available():
        vram_mb = torch.cuda.memory_allocated() / (1024 * 1024)

    result = BenchmarkResult(
        engine_name=engine_name,
        model_version=vton_out.model_version,
        sample_id="synth_sample_01",
        ssim_score=round(ssim_val, 4),
        mae_score=round(mae_val, 4),
        garment_fidelity=round(scores.garment_score, 4),
        latency_ms=round(latency_ms, 2),
        vram_mb=round(vram_mb, 2)
    )

    report_md = BenchmarkReport.generate_markdown([result])
    BenchmarkReport.save_report([result], "benchmark_report.md")
        
    print(f"Benchmark finished cleanly in {latency_ms:.2f}ms. Report generated:\n")
    print(report_md)

if __name__ == "__main__":
    run_benchmark()
