import os
import argparse
from huggingface_hub import hf_hub_download, snapshot_download

def download_fashn_weights(weights_dir: str = "./weights"):
    """
    Automated downloader for official FASHN VTON v1.5 weights from Hugging Face.
    Downloads:
      - model.safetensors (~1.9 GB)
      - dwpose/yolox_l.onnx
      - dwpose/dw-ll_ucoco_384.onnx
    """
    print(f"Pre-downloading official FASHN VTON v1.5 weights to '{weights_dir}'...")
    os.makedirs(weights_dir, exist_ok=True)
    dwpose_dir = os.path.join(weights_dir, "dwpose")
    os.makedirs(dwpose_dir, exist_ok=True)

    repo_id = "fashn-ai/fashn-vton-1.5"

    try:
        # Download model.safetensors
        print(f"Downloading model.safetensors from {repo_id}...")
        hf_hub_download(repo_id=repo_id, filename="model.safetensors", local_dir=weights_dir)
        
        # Download dwpose ONNX files
        print(f"Downloading dwpose/yolox_l.onnx from {repo_id}...")
        hf_hub_download(repo_id=repo_id, filename="dwpose/yolox_l.onnx", local_dir=weights_dir)
        
        print(f"Downloading dwpose/dw-ll_ucoco_384.onnx from {repo_id}...")
        hf_hub_download(repo_id=repo_id, filename="dwpose/dw-ll_ucoco_384.onnx", local_dir=weights_dir)

        print(f"FASHN VTON v1.5 weights successfully downloaded to {os.path.abspath(weights_dir)}.")
    except Exception as e:
        print(f"Hugging Face weight download note: {e}")
        print("Models will be fetched on demand during startup or runtime if weights directory is present.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Download FASHN VTON v1.5 Model Weights")
    parser.add_argument("--weights-dir", type=str, default="./weights", help="Target directory for weights")
    args = parser.parse_args()
    download_fashn_weights(weights_dir=args.weights_dir)
