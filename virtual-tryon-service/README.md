# Virtual Try-On AI Microservice (`virtual-tryon-service`)

Production-grade, model-agnostic, GPU-optimized **Virtual Try-On AI Service** for Fashion Pin built on Python 3.12+, FastAPI, PyTorch, CUDA, OpenCV, and Kafka.

---

## 1. Primary Architecture

- **Model Agnostic Abstraction**: Abstract `VTONEngine` class isolates model loading and inference logic. Initial engine `CatVTONEngine` supports mixed-precision FP16/BF16 GPU execution (`torch.inference_mode()`) with CPU fallback.
- **REST Endpoints**:
  - `POST /api/v1/virtual-try-on` (Create VTON Job)
  - `GET /api/v1/virtual-try-on/{job_id}` (Query Job Status & Results)
  - `GET /health` (Liveness Check)
  - `GET /ready` (Readiness Check)
  - `GET /metrics` (GPU & Memory Metrics)
- **Garment Category Strategy**: Supports `UPPER_BODY`, `LOWER_BODY`, `DRESS`, `FULL_BODY`.
- **Protected Region Preserver**: Alpha-feathered compositor (`FINAL = ORIGINAL_PROTECTED_REGIONS + GENERATED_CLOTHING_REGION`) preserves face, hair, hands, and unaffected skin.
- **Quality Engine & Retry**: Quality scoring (identity, garment, structure, artifacts) with max 2 attempts and parameter tuning.
- **Kafka Integration**: Idempotent event consumer (`fashionpin.vton.requested.v1`) and publisher (`vton.processing`, `vton.completed`, `vton.failed`).

---

## 2. Local Setup & Execution

```bash
# Navigate to service directory
cd virtual-tryon-service

# Create virtual environment & install dependencies
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Run local development server
python3 -m app.main
```

---

## 3. Running Test Suite & Benchmark

```bash
# Run unit & integration tests
pytest tests/

# Run VTON engine benchmark
python3 -m benchmark.runner
```

---

## 4. Docker Build & Deployment

```bash
# Build production NVIDIA CUDA Docker image
docker build -t fashionpin/virtual-tryon-service:latest -f Dockerfile .

# Run Docker container with GPU support
docker run --gpus all -p 8092:8092 fashionpin/virtual-tryon-service:latest
```
