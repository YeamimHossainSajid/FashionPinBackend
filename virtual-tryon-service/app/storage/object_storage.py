import os
import uuid
from typing import Optional
from PIL import Image
import boto3
from botocore.client import Config
from app.core.config import settings
from app.core.logging import logger

class ObjectStorageClient:
    """
    MinIO / AWS S3 compatible Object Storage client with signed URL generation & local fallback.
    """

    def __init__(self):
        self.storage_type = settings.storage_type
        self.bucket = settings.storage_bucket
        self.local_dir = settings.storage_directory

        if self.storage_type == "s3":
            self.s3_client = boto3.client(
                "s3",
                endpoint_url=settings.s3_endpoint_url,
                aws_access_key_id=settings.aws_access_key_id,
                aws_secret_access_key=settings.aws_secret_access_key,
                config=Config(signature_version="s3v4")
            )
        else:
            os.makedirs(self.local_dir, exist_ok=True)
            self.s3_client = None

    def upload_image(self, image: Image.Image, filename_prefix: str = "vton", max_retries: int = 3) -> str:
        key = f"{filename_prefix}_{uuid.uuid4().hex}.png"

        if self.storage_type == "s3":
            import io
            import time
            buf = io.BytesIO()
            image.save(buf, format="PNG")
            buf.seek(0)

            for attempt in range(1, max_retries + 1):
                try:
                    buf.seek(0)
                    self.s3_client.upload_fileobj(
                        buf,
                        self.bucket,
                        key,
                        ExtraArgs={"ContentType": "image/png"}
                    )
                    logger.info(f"Uploaded image to S3 bucket={self.bucket} key={key}")
                    return self.get_signed_url(key)
                except Exception as e:
                    if attempt == max_retries:
                        logger.error(f"S3 upload failed after {max_retries} attempts for key={key}: {e}")
                        raise
                    sleep_time = 0.5 * (2 ** (attempt - 1))
                    logger.warning(f"S3 upload attempt {attempt} failed, retrying in {sleep_time:.2f}s: {e}")
                    time.sleep(sleep_time)
        else:
            filepath = os.path.join(self.local_dir, key)
            image.save(filepath, format="PNG")
            logger.info(f"Saved image to local storage filepath={filepath}")
            return f"/storage/media/{key}"

    def download_image(self, image_url_or_key: str) -> Image.Image:
        import io
        if image_url_or_key.startswith("http://") or image_url_or_key.startswith("https://"):
            import httpx
            response = httpx.get(image_url_or_key, timeout=15.0)
            response.raise_for_status()
            return Image.open(io.BytesIO(response.content)).convert("RGB")
        elif os.path.isabs(image_url_or_key) or os.path.exists(image_url_or_key):
            return Image.open(image_url_or_key).convert("RGB")
        elif self.storage_type == "s3" and self.s3_client is not None:
            # Handle direct S3 object key or s3:// URI
            key = image_url_or_key
            if key.startswith("s3://"):
                parts = key[5:].split("/", 1)
                bucket_name = parts[0]
                key = parts[1] if len(parts) > 1 else ""
            else:
                bucket_name = self.bucket

            try:
                s3_resp = self.s3_client.get_object(Bucket=bucket_name, Key=key)
                return Image.open(io.BytesIO(s3_resp["Body"].read())).convert("RGB")
            except Exception as e:
                logger.error(f"Failed to retrieve image from S3 key={key}: {e}")
                raise FileNotFoundError(f"S3 image key not found: {key}") from e
        else:
            filename = os.path.basename(image_url_or_key)
            local_path = os.path.join(self.local_dir, filename)
            if os.path.exists(local_path):
                return Image.open(local_path).convert("RGB")
            raise FileNotFoundError(f"Image not found at path or key: {image_url_or_key}")

    def get_signed_url(self, key: str, expires_in: int = 3600) -> str:
        if self.storage_type == "s3":
            return self.s3_client.generate_presigned_url(
                "get_object",
                Params={"Bucket": self.bucket, "Key": key},
                ExpiresIn=expires_in
            )
        return f"/storage/media/{key}"

storage_client = ObjectStorageClient()
