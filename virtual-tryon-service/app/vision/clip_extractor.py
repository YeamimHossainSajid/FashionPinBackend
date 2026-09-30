import io
import math
from typing import List, Optional, Union
import numpy as np
from PIL import Image
from app.core.logging import logger

class VisionFeatureExtractor:
    """
    Extracts dense visual embeddings for fashion image search, reverse product lookup,
    and visual recommendation similarity.
    Produces L2-normalized 512-dimensional embedding vectors.
    """

    EMBEDDING_DIM = 512

    def __init__(self, model_name: str = "ViT-B-32", pretrained: str = "laion2b_s34b_b79k"):
        self.model_name = model_name
        self.pretrained = pretrained
        self._model = None
        self._preprocess = None
        self._device = "cpu"
        self._initialize_model()

    def _initialize_model(self) -> None:
        """
        Attempts to load open_clip or torchvision models, with fallback to an optimized feature projector.
        """
        try:
            import torch
            if torch.cuda.is_available():
                self._device = "cuda"
            elif hasattr(torch.backends, "mps") and torch.backends.mps.is_available():
                self._device = "mps"
            else:
                self._device = "cpu"

            try:
                import open_clip
                model, _, preprocess = open_clip.create_model_and_transforms(
                    self.model_name,
                    pretrained=self.pretrained,
                    device=self._device
                )
                model.eval()
                self._model = model
                self._preprocess = preprocess
                logger.info(f"Loaded OpenCLIP vision model '{self.model_name}' on device={self._device}")
                return
            except ImportError:
                logger.info("open_clip not found, attempting torchvision backbone...")

            import torchvision.models as models
            import torchvision.transforms as transforms
            # Fallback to ResNet50 vision encoder
            resnet = models.resnet50(weights=models.ResNet50_Weights.DEFAULT if hasattr(models, "ResNet50_Weights") else None)
            resnet.fc = torch.nn.Linear(resnet.fc.in_features, self.EMBEDDING_DIM)
            resnet.to(self._device)
            resnet.eval()
            self._model = resnet
            self._preprocess = transforms.Compose([
                transforms.Resize(256),
                transforms.CenterCrop(224),
                transforms.ToTensor(),
                transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
            ])
            logger.info("Loaded torchvision ResNet50 visual backbone for feature extraction")
        except Exception as e:
            logger.warning(f"Could not load deep learning vision model ({e}). Using deterministic analytical projector.")
            self._model = None

    def extract_embedding(self, image: Union[Image.Image, bytes]) -> List[float]:
        """
        Extracts a 512-dimensional normalized embedding vector from a PIL Image or image bytes.
        """
        if isinstance(image, bytes):
            image = Image.open(io.BytesIO(image))

        image_rgb = image.convert("RGB")

        if self._model is not None:
            try:
                import torch
                tensor = self._preprocess(image_rgb).unsqueeze(0).to(self._device)
                with torch.no_grad():
                    if hasattr(self._model, "encode_image"):
                        features = self._model.encode_image(tensor)
                    else:
                        features = self._model(tensor)
                    # L2 normalize
                    features = features / features.norm(dim=-1, keepdim=True)
                    embedding = features.cpu().squeeze(0).numpy().tolist()
                    return [round(float(x), 6) for x in embedding]
            except Exception as ex:
                logger.warning(f"Deep learning feature extraction failed ({ex}), falling back to analytical embedding.")

        return self._extract_analytical_embedding(image_rgb)

    def extract_batch(self, images: List[Union[Image.Image, bytes]]) -> List[List[float]]:
        """
        Extract embeddings for a batch of images.
        """
        return [self.extract_embedding(img) for img in images]

    def _extract_analytical_embedding(self, image: Image.Image) -> List[float]:
        """
        High-dimensional deterministic feature projection combining spatial multi-scale HSV
        and gradient statistics when deep weights are unavailable.
        """
        resized = image.resize((128, 128), Image.Resampling.BILINEAR)
        img_np = np.array(resized, dtype=np.float32) / 255.0

        # Multi-region color histogram
        quadrants = [
            img_np[:64, :64],
            img_np[:64, 64:],
            img_np[64:, :64],
            img_np[64:, 64:],
        ]

        feature_vector = []
        for quad in quadrants:
            for c in range(3):
                hist, _ = np.histogram(quad[:, :, c], bins=16, range=(0.0, 1.0))
                feature_vector.extend(hist.tolist())

        # Grayscale spatial gradients
        gray = 0.2989 * img_np[:, :, 0] + 0.5870 * img_np[:, :, 1] + 0.1140 * img_np[:, :, 2]
        grad_x = np.diff(gray, axis=1)
        grad_y = np.diff(gray, axis=0)

        gx_hist, _ = np.histogram(grad_x, bins=32, range=(-0.5, 0.5))
        gy_hist, _ = np.histogram(grad_y, bins=32, range=(-0.5, 0.5))
        feature_vector.extend(gx_hist.tolist())
        feature_vector.extend(gy_hist.tolist())

        # Pad or project deterministically to exactly 512 dims
        vec = np.array(feature_vector, dtype=np.float32)
        if len(vec) < self.EMBEDDING_DIM:
            # Deterministic pseudo-random projection to 512
            rng = np.random.RandomState(42)
            proj_matrix = rng.randn(len(vec), self.EMBEDDING_DIM).astype(np.float32)
            emb = np.dot(vec, proj_matrix)
        else:
            emb = vec[:self.EMBEDDING_DIM]

        # L2 Normalization
        norm = np.linalg.norm(emb)
        if norm > 1e-8:
            emb = emb / norm
        else:
            emb = np.zeros(self.EMBEDDING_DIM, dtype=np.float32)

        return [round(float(x), 6) for x in emb.tolist()]

    @staticmethod
    def cosine_similarity(vec1: List[float], vec2: List[float]) -> float:
        """
        Computes cosine similarity between two normalized feature vectors.
        """
        a = np.array(vec1, dtype=np.float32)
        b = np.array(vec2, dtype=np.float32)
        denom = (np.linalg.norm(a) * np.linalg.norm(b))
        if denom < 1e-8:
            return 0.0
        return float(np.clip(np.dot(a, b) / denom, -1.0, 1.0))
