import os
from pydantic_settings import BaseSettings
from pydantic import ConfigDict, Field

class Settings(BaseSettings):
    model_config = ConfigDict(env_file=".env", extra="ignore")

    service_name: str = Field(default="virtual-tryon-service", alias="SERVICE_NAME")
    service_port: int = Field(default=8092, alias="SERVICE_PORT")
    debug: bool = Field(default=False, alias="DEBUG")
    env: str = Field(default="development", alias="ENV")

    model_name: str = Field(default="fashn-vton", alias="MODEL_NAME")
    model_version: str = Field(default="1.5", alias="MODEL_VERSION")
    pipeline_version: str = Field(default="1.0.0", alias="PIPELINE_VERSION")
    model_path: str = Field(default="./weights", alias="MODEL_PATH")
    device: str = Field(default="cuda", alias="DEVICE")
    dtype: str = Field(default="bfloat16", alias="DTYPE")

    VTON_MODEL_PATH: str = Field(default="./weights", alias="VTON_MODEL_PATH")
    VTON_MODEL_VERSION: str = Field(default="1.5", alias="VTON_MODEL_VERSION")
    VTON_DEVICE: str = Field(default="cuda", alias="VTON_DEVICE")
    VTON_DTYPE: str = Field(default="bfloat16", alias="VTON_DTYPE")

    image_width: int = Field(default=768, alias="IMAGE_WIDTH")
    image_height: int = Field(default=1024, alias="IMAGE_HEIGHT")
    max_attempts: int = Field(default=2, alias="MAX_ATTEMPTS")
    quality_threshold: float = Field(default=0.85, alias="QUALITY_THRESHOLD")

    storage_type: str = Field(default="local", alias="STORAGE_TYPE")
    storage_bucket: str = Field(default="fashionpin-media", alias="STORAGE_BUCKET")
    storage_directory: str = Field(default="./storage/media", alias="STORAGE_DIRECTORY")
    s3_endpoint_url: str = Field(default="http://localhost:9000", alias="S3_ENDPOINT_URL")
    aws_access_key_id: str = Field(default="minioadmin", alias="AWS_ACCESS_KEY_ID")
    aws_secret_access_key: str = Field(default="minioadmin", alias="AWS_SECRET_ACCESS_KEY")

    kafka_bootstrap_servers: str = Field(default="localhost:9092", alias="KAFKA_BOOTSTRAP_SERVERS")
    kafka_topic_requested: str = Field(default="fashionpin.vton.requested.v1", alias="KAFKA_TOPIC_REQUESTED")
    kafka_topic_completed: str = Field(default="fashionpin.vton.completed.v1", alias="KAFKA_TOPIC_COMPLETED")
    kafka_topic_failed: str = Field(default="fashionpin.vton.failed.v1", alias="KAFKA_TOPIC_FAILED")
    kafka_group_id: str = Field(default="virtual-tryon-service", alias="KAFKA_GROUP_ID")

    temp_directory: str = Field(default="/tmp/vton", alias="TEMP_DIRECTORY")

settings = Settings()
