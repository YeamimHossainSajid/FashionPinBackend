# Virtual Try-On AI Microservice (`virtual-tryon-service`)

Production-grade, model-agnostic, GPU-optimized **Virtual Try-On AI Service** for Fashion Pin built on Python 3.12+, FastAPI, PyTorch, CUDA, OpenCV, and Kafka, featuring official **FASHN VTON v1.5** integration (`FashnVTONEngine`).

---

## 1. Primary Architecture

- **Model Agnostic Abstraction**: Abstract `VTONEngine` class isolates model loading and inference logic.
  - **FashnVTONEngine**: Official FASHN VTON v1.5 implementation featuring MMDiT `TryOnModel`, DWPose ONNX keypoints, and `FashnHumanParser`.
  - **CatVTONEngine**: Baseline lightweight CatVTON implementation.
- **REST Endpoints**:
  - `POST /api/v1/virtual-try-on` (Create VTON Job)
  - `GET /api/v1/virtual-try-on/{job_id}` (Query Job Status & Results)
  - `GET /health` (Liveness Check)
  - `GET /ready` (Readiness Check)
  - `GET /metrics` (GPU & Memory Metrics)
- **Garment Categories**: Supports `tops`, `bottoms`, `one-pieces`, `UPPER_BODY`, `LOWER_BODY`, `DRESS`, `FULL_BODY`.
- **Garment Photo Types**: Supports `model` (model-worn garment) and `flat-lay` (product shot).
- **Protected Region Preserver**: Alpha-feathered compositor (`FINAL = ORIGINAL_PROTECTED_REGIONS + GENERATED_CLOTHING_REGION`) preserves face, hair, hands, and unaffected skin.
- **Quality Engine & Retry**: Quality scoring (identity, garment, structure, artifacts) with max 2 attempts and parameter tuning.
- **Kafka Integration**: Idempotent event consumer (`fashionpin.vton.requested.v1`) and publisher (`vton.processing`, `vton.completed`, `vton.failed`).

---

## 2. Local Setup & Weight Pre-downloading

```bash
# Navigate to service directory
cd virtual-tryon-service

# Create virtual environment & install dependencies
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Pre-download FASHN VTON v1.5 weights (~1.9 GB)
python3 scripts/download_weights.py --weights-dir ./weights

# Run local development server
python3 -m app.main
```

---

## 3. Running Test Suite & Benchmark

```bash
# Run unit & integration tests
PYTHONPATH=. ./venv/bin/pytest tests/

# Run VTON engine benchmark
PYTHONPATH=. ./venv/bin/python3 -m benchmark.runner
```

---

## 4. Third-Party Licenses & Inventory

See [`LICENSES/THIRD_PARTY.md`](LICENSES/THIRD_PARTY.md) for full licensing details:
- **FASHN VTON v1.5 Core**: Apache-2.0
- **DWPose**: Apache-2.0
- **YOLOX**: Apache-2.0
- **fashn-human-parser**: MIT License

---

## 5. Docker Build & Deployment

```bash
# Build production NVIDIA CUDA Docker image
docker build -t fashionpin/virtual-tryon-service:latest -f Dockerfile .

# Run Docker container with GPU support
docker run --gpus all -p 8092:8092 fashionpin/virtual-tryon-service:latest
```
