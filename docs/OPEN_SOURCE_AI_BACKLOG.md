# 🧠 Kymira AI & Computer Vision Open Source Issue Backlog

A curated collection of issues for contributors, ranging from beginner Good First Issues to intermediate computer vision tasks and advanced diffusion model engineering. All issues focus exclusively on the Python AI and Machine Learning components (`virtual-tryon-service`, image search, and visual embeddings).

---

## 🟢 Beginner (Good First Issues)

### Issue #1: Add Automated WebP Decoding and RGBA Alpha Flattening in InputValidator
* **Labels**: `good first issue`, `python`, `computer vision`, `bug`
* **Target File**: [`virtual-tryon-service/app/preprocessing/input_validator.py`](file:///Users/sajid/Documents/FashionPinFullStack/FashionPinBackend/virtual-tryon-service/app/preprocessing/input_validator.py)
* **Problem**: When users upload PNG or WebP images with transparent backgrounds (`RGBA` or `LA` color modes), direct conversion via `.convert("RGB")` can produce black background artifacts or decoding exceptions.
* **Proposed Solution**:
  1. Inspect incoming image mode in `InputValidator.validate_image_bytes`.
  2. If the image has an alpha channel, composite it over a clean solid white background before converting to `RGB`.
  3. Ensure animated WebP files extract the primary frame cleanly without corruption.
* **Acceptance Criteria**:
  * Unit test verifies transparent PNGs and WebP images convert to RGB with white backgrounds.
  * No `OSError` thrown on valid WebP inputs.

---

### Issue #2: Build Comprehensive Pytest Unit Test Suite for QualityEngine Scorer
* **Labels**: `good first issue`, `testing`, `python`, `quality`
* **Target File**: `virtual-tryon-service/tests/test_quality_scorer.py`
* **Problem**: The `QualityEngine` evaluates identity preservation, garment color fidelity, and structural integrity, but currently lacks automated unit tests covering threshold edge cases.
* **Proposed Solution**:
  1. Create `tests/test_quality_scorer.py` with synthetic numpy arrays and dummy PIL images.
  2. Test `_compute_identity_score` with matching versus completely blacked-out masks.
  3. Test `_compute_garment_score` with matching versus contrasting color histograms.
  4. Verify `passes_gate()` correctly approves and rejects score payloads based on configured thresholds.
* **Acceptance Criteria**:
  * `pytest tests/test_quality_scorer.py` achieves 90%+ code coverage on `app/quality/scorer.py`.

---

### Issue #3: Implement Structured Correlation ID Logging in FastAPI Pipeline
* **Labels**: `good first issue`, `fastapi`, `observability`
* **Target Files**:
  * [`virtual-tryon-service/app/core/logging.py`](file:///Users/sajid/Documents/FashionPinFullStack/FashionPinBackend/virtual-tryon-service/app/core/logging.py)
  * [`virtual-tryon-service/app/main.py`](file:///Users/sajid/Documents/FashionPinFullStack/FashionPinBackend/virtual-tryon-service/app/main.py)
* **Problem**: When requests pass through the Spring Cloud API Gateway into the Python virtual try-on service, the `X-Correlation-Id` header is received but not automatically appended to internal Python log records.
* **Proposed Solution**:
  1. Add a FastAPI middleware in `main.py` that extracts `X-Correlation-Id` (or generates a UUID if absent).
  2. Use Python `contextvars` to store the correlation ID per request.
  3. Update `logger` formatter to output structured JSON including `correlation_id`, `timestamp`, and `level`.
* **Acceptance Criteria**:
  * Every log entry emitted during a try-on job contains `"correlation_id": "..."`.

---

### Issue #4: Implement In-Memory SHA-256 Caching for Preprocessed Garment Masks
* **Labels**: `good first issue`, `performance`, `caching`
* **Target File**: [`virtual-tryon-service/app/preprocessing/garment/processor.py`](file:///Users/sajid/Documents/FashionPinFullStack/FashionPinBackend/virtual-tryon-service/app/preprocessing/garment/processor.py)
* **Problem**: In e-commerce workflows, hundreds of users try on the exact same garment SKU. Running U-2-Net garment segmentation repeatedly on identical clothing photos wastes GPU cycles.
* **Proposed Solution**:
  1. Compute SHA-256 hash of incoming garment image bytes.
  2. Implement an LRU cache or local dictionary cache for extracted garment cutouts and binary masks.
  3. Return cached segmentations directly when a matching hash is detected.
* **Acceptance Criteria**:
  * Identical garment inputs bypass U-2-Net inference on repeat invocations.

---

## 🟡 Intermediate (Computer Vision & Model Optimization)

### Issue #5: Implement OpenCLIP (ViT-B/32) Feature Extractor for Visual Image Search
* **Labels**: `enhancement`, `computer vision`, `embeddings`, `feature`
* **Target Directory**: `virtual-tryon-service/app/vision/`
* **Problem**: Users need to search the luxury catalog by uploading an image or screenshot ("Shop the Look"). We need a high-performance visual embedding extractor in Python.
* **Proposed Solution**:
  1. Create `app/vision/clip_extractor.py` using `open_clip_torch` with model `ViT-B-32` (`laion2b_s34b_b79k`).
  2. Implement `extract_embedding(image: Image.Image) -> List[float]` returning normalized 512-dimension float vectors.
  3. Add batch inference support: `extract_batch(images: List[Image.Image]) -> np.ndarray`.
  4. Expose a FastAPI endpoint `POST /api/v1/vision/embed` accepting multipart image uploads.
* **Acceptance Criteria**:
  * Extracts normalized L2 embeddings within 50ms on CPU or 8ms on GPU.
  * Vector cosine similarity between similar dresses scores > 0.82.

---

### Issue #6: Add Segment Anything Model (SAM 2) Zero-Shot Garment Mask Refinement
* **Labels**: `enhancement`, `deep learning`, `segmentation`
* **Target File**: [`virtual-tryon-service/app/preprocessing/garment/segmentation.py`](file:///Users/sajid/Documents/FashionPinFullStack/FashionPinBackend/virtual-tryon-service/app/preprocessing/garment/segmentation.py)
* **Problem**: Standard U-2-Net segmentation frequently cuts off fine garment details such as sheer lace collars, thin spaghetti straps, and fringe hems.
* **Proposed Solution**:
  1. Integrate `segment-anything-2` (SAM 2 tiny/small checkpoint) for garment boundary refinement.
  2. Use U-2-Net bounding boxes as bounding prompts for SAM 2.
  3. Generate ultra-crisp alpha masks preserving delicate fabric edges.
* **Acceptance Criteria**:
  * Complex garment silhouettes with sheer fabrics achieve higher IoU (> 0.94) against ground truth cutouts.

---

### Issue #7: Accelerate DWPose ONNXRuntime with FP16 Precision & CUDA Execution Provider
* **Labels**: `performance`, `onnx`, `gpu`, `optimization`
* **Target File**: [`virtual-tryon-service/app/vton/engines/fashn/dwpose/onnxpose.py`](file:///Users/sajid/Documents/FashionPinFullStack/FashionPinBackend/virtual-tryon-service/app/vton/engines/fashn/dwpose/onnxpose.py)
* **Problem**: Full-body pose estimation runs in standard FP32 on CPU by default, which creates a latency bottleneck during high-throughput try-on requests.
* **Proposed Solution**:
  1. Detect available providers (`CUDAExecutionProvider`, `TensorrtExecutionProvider`, `CPUExecutionProvider`).
  2. Configure session options with `onnxruntime.GraphOptimizationLevel.ORT_ENABLE_ALL`.
  3. Support FP16 ONNX weights for 2x faster inference and 50% memory reduction on NVIDIA GPUs.
* **Acceptance Criteria**:
  * DWPose inference completes in under 25ms on modern GPU devices.

---

### Issue #8: Implement Adaptive Gaussian Edge Feathering for Person Parser Masks
* **Labels**: `enhancement`, `computer vision`, `postprocessing`
* **Target File**: [`virtual-tryon-service/app/preprocessing/person/mask.py`](file:///Users/sajid/Documents/FashionPinFullStack/FashionPinBackend/virtual-tryon-service/app/preprocessing/person/mask.py)
* **Problem**: Hard binary borders between human skin and warped clothing create visible jagged lines in rendered virtual try-on photos.
* **Proposed Solution**:
  1. Implement morphological gradient and guided filter smoothing on agnostic masks.
  2. Apply distance transform based feathering (`cv2.distanceTransform`) around neck and wrist boundaries.
  3. Ensure smooth alpha gradients across transition zones while preserving 100% opacity on core fabric.
* **Acceptance Criteria**:
  * Visual inspection shows zero stepped pixel artifacts at neckline and waistline junctions.

---

### Issue #9: Add FAISS In-Memory Vector Search Index for Sub-10ms Catalog Lookups
* **Labels**: `enhancement`, `search`, `vector db`, `faiss`
* **Target Directory**: `virtual-tryon-service/app/search/`
* **Problem**: Querying PostgreSQL for nearest-neighbor vector embeddings becomes slow when catalog sizes exceed 100,000 product images.
* **Proposed Solution**:
  1. Build `app/search/faiss_index.py` wrapping `faiss.IndexHNSWFlat`.
  2. Implement methods for `add_items(ids: List[str], vectors: np.ndarray)` and `search(query_vector: np.ndarray, top_k: int = 20)`.
  3. Implement thread-safe index saves and loads to local storage / MinIO.
* **Acceptance Criteria**:
  * Top-20 nearest neighbor lookup executes in under 5ms on a 500,000 vector index.

---

## 🔴 Advanced (Diffusion Models & Research Engineering)

### Issue #10: Integrate Dynamic LoRA Adapter Weight Loading in CatVTON Diffusion Engine
* **Labels**: `advanced`, `diffusion models`, `catvton`, `pytorch`
* **Target File**: [`virtual-tryon-service/app/vton/engines/catvton/loader.py`](file:///Users/sajid/Documents/FashionPinFullStack/FashionPinBackend/virtual-tryon-service/app/vton/engines/catvton/loader.py)
* **Problem**: Different fashion categories (heavy denim, chunky wool knits, glossy leather, sheer silk) require specialized texture guidance that generic diffusion weights often smooth out.
* **Proposed Solution**:
  1. Add LoRA weight loading utility using `peft` or `diffusers.loaders.LoraLoaderMixin`.
  2. Allow API requests to specify `lora_adapter: "silk_v1"` or `lora_adapter: "denim_v2"`.
  3. Fuse LoRA weights into UNet attention layers dynamically with configurable alpha blending.
* **Acceptance Criteria**:
  * LoRA weights load and fuse in under 500ms without restarting the service process.
  * Visual outputs maintain intricate denim twill and knit stitching patterns.

---

### Issue #11: Replace Heuristic Blurriness Checks with LPIPS Perceptual Fidelity Scoring
* **Labels**: `advanced`, `pytorch`, `quality scoring`, `metrics`
* **Target File**: [`virtual-tryon-service/app/quality/artifact_detection.py`](file:///Users/sajid/Documents/FashionPinFullStack/FashionPinBackend/virtual-tryon-service/app/quality/artifact_detection.py)
* **Problem**: Laplacian variance checks detect simple image blur but cannot detect subtle generative hallucinations (e.g. garbled patterns, extra fingers, or distorted buttons).
* **Proposed Solution**:
  1. Integrate `lpips` (Learned Perceptual Image Patch Similarity) with VGG backbone in PyTorch.
  2. Compare high-frequency feature maps between input garment and rendered clothing area.
  3. Calibrate threshold score so generative distortions trigger automatic retry flags.
* **Acceptance Criteria**:
  * Perceptual fidelity score correlates accurately with manual human grading of fabric distortion.

---

### Issue #12: Build Intelligent Micro-Batching Scheduler for Asynchronous VTON Inference
* **Labels**: `advanced`, `concurrency`, `gpu`, `performance`
* **Target File**: [`virtual-tryon-service/app/jobs/processor.py`](file:///Users/sajid/Documents/FashionPinFullStack/FashionPinBackend/virtual-tryon-service/app/jobs/processor.py)
* **Problem**: Processing try-on requests one by one underutilizes modern GPU tensor cores, leaving compute efficiency below 35%.
* **Proposed Solution**:
  1. Build an asynchronous queue worker with a 40ms sliding accumulation window.
  2. Collate up to 4 incoming requests into a batched PyTorch tensor (`B, C, H, W`).
  3. Execute single batched forward pass through diffusion UNet.
  4. Scatter results back to respective client futures / Kafka response topics.
* **Acceptance Criteria**:
  * GPU compute throughput increases by 2.2x under sustained concurrent load.
