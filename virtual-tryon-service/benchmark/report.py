import json
from typing import List
from benchmark.metrics import BenchmarkResult

class BenchmarkReport:
    @classmethod
    def generate_markdown(cls, results: List[BenchmarkResult]) -> str:
        md = "# VTON Engine Benchmark Performance Report\n\n"
        md += "| Engine | Sample ID | SSIM | MAE | Garment Fidelity | Latency (ms) | VRAM (MB) |\n"
        md += "| :--- | :--- | :--- | :--- | :--- | :--- | :--- |\n"
        for r in results:
            md += f"| {r.engine_name} ({r.model_version}) | {r.sample_id} | {r.ssim_score:.4f} | {r.mae_score:.4f} | {r.garment_fidelity:.4f} | {r.latency_ms:.2f} | {r.vram_mb:.2f} |\n"
        return md

    @classmethod
    def save_report(cls, results: List[BenchmarkResult], output_path: str = "benchmark_report.md"):
        md_content = cls.generate_markdown(results)
        with open(output_path, "w") as f:
            f.write(md_content)
        return md_content
