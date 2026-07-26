from pydantic import BaseModel, Field
from typing import Optional, Literal
import numpy as np

ProcessType = Literal["Moran", "Local", "Fermi"]

class SimRequest(BaseModel):
    matrix: list[list[float]]          # 4x4 payoff matrix
    pop_size: int = 100
    iterations: int = 10000
    simulations: int = 50
    w: float = 0.45
    process: ProcessType = "Moran"
    initial_dist: Optional[list[float]] = None
    initial_rand: bool = False
    traj: bool = True
    point_cloud: bool = False

class ReplicatorRequest(BaseModel):
    matrix: list[list[float]]          # 4x4
    process: ProcessType = "Moran"
    w: float = 0.45
    initial_dist: list[float] = [0.5, 0.2, 0.2]
    time_span: float = 150.0

class PDRequest(BaseModel):
    matrix: list[list[float]]          # 2x2
    w: float = 0.9
    initial_dist: list[float] = [0.9, 0.1]
    adjusted: bool = False             # True = Moran adjusted, False = local/standard

class CriticalNRequest(BaseModel):
    matrix: list[list[float]]
    w: float = 0.45
    process: ProcessType = "Local"
    n_lo: float = 5.0
    n_hi: float = 5000.0

class DeltaHRangeRequest(BaseModel):
    matrix: list[list[float]]
    w: float = 0.45
    process: ProcessType = "Local"
    n_min: float = 10.0
    n_max: float = 1500.0
    n_points: int = 25

class FixedPointRequest(BaseModel):
    matrix: list[list[float]]
    w: float = 0.45