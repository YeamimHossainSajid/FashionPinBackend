# Third-Party Licenses & Dependency Inventory

This document details all third-party software components, models, and dependencies integrated into the **Fashion Pin Virtual Try-On AI Service** (`virtual-tryon-service`).

---

## 1. FASHN VTON v1.5 Core
- **Upstream Repository**: [https://github.com/fashn-AI/fashn-vton-1.5](https://github.com/fashn-AI/fashn-vton-1.5)
- **Model Checkpoints**: [https://huggingface.co/fashn-ai/fashn-vton-1.5](https://huggingface.co/fashn-ai/fashn-vton-1.5)
- **License**: Apache License 2.0
- **Commercial Use**: Allowed under Apache 2.0 terms with copyright and license notices preserved.

---

## 2. Integrated Third-Party Runtime Components

### DWPose (DensePose & Keypoint Detection)
- **Source**: [https://github.com/IDEA-Research/DWPose](https://github.com/IDEA-Research/DWPose)
- **License**: Apache License 2.0
- **ONNX Weights**: `dwpose/dw-ll_ucoco_384.onnx`
- **Commercial Use**: Allowed under Apache 2.0.

### YOLOX (Person & Pose Bounding Box Detection)
- **Source**: [https://github.com/Megvii-BaseDetection/YOLOX](https://github.com/Megvii-BaseDetection/YOLOX)
- **License**: Apache License 2.0
- **ONNX Weights**: `dwpose/yolox_l.onnx`
- **Commercial Use**: Allowed under Apache 2.0.

### fashn-human-parser
- **Source**: [https://github.com/fashn-AI/fashn-human-parser](https://github.com/fashn-AI/fashn-human-parser)
- **License**: MIT License / HuggingFace Weights
- **Commercial Use**: Allowed under MIT License terms with copyright and permission notices included.

---

## 3. Required Attribution & Notices

- **FASHN VTON v1.5**: Copyright (c) 2026 FASHN AI (Dan Bochman & Aya Bochman). Licensed under Apache 2.0.
- **DWPose**: Copyright (c) IDEA Research. Licensed under Apache 2.0.
- **YOLOX**: Copyright (c) Megvii Inc. Licensed under Apache 2.0.
- **fashn-human-parser**: Copyright (c) 2026 FASHN AI. Licensed under MIT.
