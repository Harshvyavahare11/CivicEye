import io
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_analyze_demo_mode_pothole():
    # 1x1 transparent PNG image
    tiny_png = (
        b'\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01'
        b'\x08\x06\x00\x00\x00\x1f\x15c4\x00\x00\x00\rIDATx\x9cc\x00\x01\x00'
        b'\x00\x05\x00\x01\r\n-\xb4\x00\x00\x00\x00IEND\xaeB`\x82'
    )
    
    files = {"image": ("test_pothole.png", io.BytesIO(tiny_png), "image/png")}
    data = {
        "description": "Deep pothole damaging vehicles on Main Street",
        "location": "Main Street, Sector 4",
        "demo_mode": "true"
    }

    response = client.post("/analyze", files=files, data=data)
    assert response.status_code == 200
    res_data = response.json()
    assert res_data["category"] == "Pothole"
    assert res_data["demoMode"] is True
    assert "severity" in res_data
    assert "observations" in res_data
    assert len(res_data["observations"]) > 0
    assert "complaintText" in res_data

def test_analyze_rejects_unsupported_file_type():
    fake_pdf = b"%PDF-1.4 Fake PDF file content"
    files = {"image": ("document.pdf", io.BytesIO(fake_pdf), "application/pdf")}
    data = {"description": "Testing rejection"}

    response = client.post("/analyze", files=files, data=data)
    assert response.status_code == 415

def test_analyze_rejects_empty_file():
    empty_file = b""
    files = {"image": ("empty.png", io.BytesIO(empty_file), "image/png")}
    data = {"description": "Empty file test"}

    response = client.post("/analyze", files=files, data=data)
    assert response.status_code == 400
