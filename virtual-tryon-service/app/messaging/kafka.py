import json
import threading
import time
from typing import Dict, Any, Set
from kafka import KafkaProducer, KafkaConsumer
from app.core.config import settings
from app.core.logging import logger
from app.api.schemas.virtual_try_on import CreateVTONJobRequest, GarmentCategory
from app.jobs.processor import job_processor

class KafkaMessagingClient:
    """
    Idempotent Kafka Consumer & Event Publisher for VTON jobs.
    Topics:
      - Requested: fashionpin.vton.requested.v1
      - Completed: fashionpin.vton.completed.v1
      - Failed:    fashionpin.vton.failed.v1
    """

    def __init__(self):
        self.bootstrap_servers = settings.kafka_bootstrap_servers
        self.topic_requested = settings.kafka_topic_requested
        self.topic_completed = settings.kafka_topic_completed
        self.topic_failed = settings.kafka_topic_failed
        self.group_id = settings.kafka_group_id

        self.producer = None
        self.consumer = None
        self._processed_event_ids: Set[str] = set()
        self._running = False
        self._consumer_thread = None

    def initialize_producer(self) -> None:
        try:
            self.producer = KafkaProducer(
                bootstrap_servers=self.bootstrap_servers,
                value_serializer=lambda v: json.dumps(v).encode("utf-8"),
                key_serializer=lambda k: k.encode("utf-8") if k else None,
                retries=3,
            )
            logger.info(f"Kafka Producer initialized for bootstrap_servers={self.bootstrap_servers}")
        except Exception as e:
            logger.warning(f"Kafka Producer initialization skipped/failed: {str(e)}")

    def publish_event(self, topic: str, key: str, payload: Dict[str, Any]) -> None:
        if self.producer is None:
            self.initialize_producer()

        if self.producer is not None:
            try:
                self.producer.send(topic, key=key, value=payload)
                self.producer.flush()
                logger.info(f"Published Kafka event key={key} to topic={topic}")
            except Exception as e:
                logger.error(f"Failed to publish Kafka event key={key} to topic={topic}: {str(e)}")

    def start_consumer(self) -> None:
        if self._running:
            return

        self._running = True
        self._consumer_thread = threading.Thread(target=self._consume_loop, daemon=True)
        self._consumer_thread.start()
        logger.info(f"Kafka Consumer thread started for topic={self.topic_requested}")

    def stop_consumer(self) -> None:
        self._running = False

    def _consume_loop(self) -> None:
        try:
            consumer = KafkaConsumer(
                self.topic_requested,
                bootstrap_servers=self.bootstrap_servers,
                group_id=self.group_id,
                auto_offset_reset="earliest",
                enable_auto_commit=True,
                value_deserializer=lambda m: json.loads(m.decode("utf-8"))
            )
            logger.info(f"Kafka Consumer connected to {self.bootstrap_servers} on group {self.group_id}")

            while self._running:
                raw_messages = consumer.poll(timeout_ms=1000)
                for topic_partition, msgs in raw_messages.items():
                    for msg in msgs:
                        self._process_message(msg.value)
        except Exception as e:
            logger.warning(f"Kafka Consumer loop encountered error or offline: {str(e)}")

    def _process_message(self, message: Dict[str, Any]) -> None:
        event_id = message.get("eventId") or message.get("event_id") or message.get("job_id")
        if event_id and event_id in self._processed_event_ids:
            logger.info(f"Duplicate event received event_id={event_id}, skipping processing")
            return

        person_url = message.get("person_image_url") or message.get("personImageUrl")
        garment_url = message.get("garment_image_url") or message.get("garmentImageUrl")
        category_str = message.get("garment_category") or message.get("garmentCategory") or "UPPER_BODY"

        if not person_url or not garment_url:
            logger.warning(f"Invalid Kafka message structure: {message}")
            return

        if event_id:
            self._processed_event_ids.add(event_id)

        category = GarmentCategory(category_str)
        req = CreateVTONJobRequest(
            person_image_url=person_url,
            garment_image_url=garment_url,
            garment_category=category
        )

        job = job_processor.create_job(req)
        self.publish_event(self.topic_completed, job.job_id, {"job_id": job.job_id, "status": "PROCESSING"})

        result_job = job_processor.execute_job(job.job_id, req)

        if result_job.status == "COMPLETED":
            self.publish_event(self.topic_completed, result_job.job_id, {
                "job_id": result_job.job_id,
                "status": "COMPLETED",
                "result_url": result_job.result_url,
                "quality_score": result_job.quality_score,
                "model_version": result_job.model_version
            })
        else:
            self.publish_event(self.topic_failed, result_job.job_id, {
                "job_id": result_job.job_id,
                "status": "FAILED",
                "reason": result_job.failure_reason
            })

kafka_client = KafkaMessagingClient()
