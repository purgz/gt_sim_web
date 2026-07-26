from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_health():
    r = client.get("/health")
    assert r.status_code == 200
    assert r.json()["status"] == "ok"

def test_sim_requires_token():
    r = client.post("/sim/run", json={
        "matrix": [[0,-0.8,1,0.4],[1,0,-0.8,0.4],[-0.8,1,0,0.4],[0.2,0.2,0.2,0]],
        "pop_size": 20, "iterations": 100, "simulations": 2
    })
    assert r.status_code == 422  # missing header

def test_sim_with_token():
    r = client.post("/sim/run",
        headers={"x-internal-token": "dev-token"},
        json={
            "matrix": [[0,-0.8,1,0.4],[1,0,-0.8,0.4],[-0.8,1,0,0.4],[0.2,0.2,0.2,0]],
            "pop_size": 20, "iterations": 200, "simulations": 2,
            "process": "Moran", "traj": True
        }
    )
    assert r.status_code == 200
    data = r.json()
    assert "avg_trajectory" in data
    assert data["n_strategies"] == 4