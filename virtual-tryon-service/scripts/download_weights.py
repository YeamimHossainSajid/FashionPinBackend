import os
from huggingface_hub import snapshot_download
from app.core.config import settings

def download_weights():
    print(f"Pre-downloading VTON model weights for '{settings.model_path}'...")
    os.makedirs("./models", exist_ok=True)
    try:
        path = snapshot_download(repo_id=settings.model_path, local_dir="./models/catvton")
        print(f"Successfully downloaded weights to {path}")
    except Exception as e:
        print(f"Failed or skipped Hugging Face download ({e}). Models will load dynamically.")

if __name__ == "__main__":
    download_weights()
