from fastapi import FastAPI, Header, HTTPException
from contextlib import asynccontextmanager
import os

from routers import simulation, replicator, analysis

INTERNAL_TOKEN = os.getenv("SIM_INTERNAL_TOKEN", "dev-token")

def verify_token(x_internal_token: str = Header(...)):
    if x_internal_token != INTERNAL_TOKEN:
        raise HTTPException(status_code=401, detail="Invalid internal token")

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("Starting sim service")
    yield

app = FastAPI(lifespan=lifespan)

app.include_router(simulation.router, prefix="/sim", tags=["simulation"])
app.include_router(replicator.router, prefix="/replicator", tags=["replicator"])
app.include_router(analysis.router, prefix="/analysis", tags=["analysis"])

@app.get("/health")
def health():
    return {"status": "ok"}