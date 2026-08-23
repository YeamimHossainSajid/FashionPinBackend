import pytest
from fastapi.testclient import TestClient
from PIL import Image
import os
import io
from app.main import app

client = TestClient(app)

def test_health_route():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "UP"

def test_metrics_route():
    response = client.get("/metrics")
    assert response.status_code == 200
    assert "engine" in response.json()

def test_ready_route():
    response = client.get("/ready")
    assert response.status_code in (200, 503)

def test_sync_virtual_try_on_flow(tmp_path):
    p_img = Image.new("RGB", (512, 512), color="white")
    g_img = Image.new("RGB", (512, 512), color="blue")
    
    p_path = str(tmp_path / "person.png")
    g_path = str(tmp_path / "garment.png")
    p_img.save(p_path)
    g_img.save(g_path)

    payload = {
        "person_image_url": p_path,
        "garment_image_url": g_path,
        "garment_category": "UPPER_BODY"
    }

    response = client.post("/api/v1/virtual-try-on?sync=true", json=payload)
    assert response.status_code == 202
    data = response.json()
    assert data["status"] == "COMPLETED"
    assert "result_url" in data
    assert data["quality_score"] > 0.0

    # Query Job Status
    job_id = data["job_id"]
    query_resp = client.get(f"/api/v1/virtual-try-on/{job_id}")
    assert query_resp.status_code == 200
    assert query_resp.json()["job_id"] == job_id
