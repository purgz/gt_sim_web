from fastapi import APIRouter, Depends, Header, HTTPException
import numpy as np
import os
from models.schemas import ReplicatorRequest, PDRequest
import replicator

router = APIRouter()

INTERNAL_TOKEN = os.getenv("SIM_INTERNAL_TOKEN", "dev-token")

def verify(x_internal_token: str = Header(...)):
    if x_internal_token != INTERNAL_TOKEN:
        raise HTTPException(status_code=403, detail="Forbidden")

@router.post("/trajectory")
def numerical_trajectory(req: ReplicatorRequest, _=Depends(verify)):
    """
    Standard replicator dynamics (numericalTrajectory).
    Returns c1,c2,c3,c4 columns + time axis.
    """
    matrix = np.array(req.matrix)
    df, t_eval = replicator.numericalTrajectory(
        interactionProcess=req.process,
        w=req.w,
        initial_dist=req.initial_dist,
        matrix=matrix,
    )
    return {
        "t": list(map(float, t_eval)),
        "c1": list(map(float, df["c1"])),
        "c2": list(map(float, df["c2"])),
        "c3": list(map(float, df["c3"])),
        "c4": list(map(float, df["c4"])),
    }

@router.post("/fokker-planck")
def fokker_planck_trajectory(req: ReplicatorRequest, _=Depends(verify)):
    """
    Trajectory derived from the Fokker-Planck equation.
    """
    matrix = np.array(req.matrix)
    df, t_eval = replicator.numerical_trajectory_from_fokker_planck(
        matrix=matrix,
        interaction_process=req.process,
        w=req.w,
        initial_dist=req.initial_dist,
        time_span=req.time_span,
    )
    return {
        "t": list(map(float, t_eval)),
        "c1": list(map(float, df["c1"])),
        "c2": list(map(float, df["c2"])),
        "c3": list(map(float, df["c3"])),
        "c4": list(map(float, df["c4"])),
    }

@router.post("/pd")
def prisoners_dilemma(req: PDRequest, _=Depends(verify)):
    """
    2x2 Prisoner's Dilemma replicator (standard or Moran-adjusted).
    """
    matrix = np.array(req.matrix)
    if req.adjusted:
        df, t_eval = replicator.pd_adjusted(
            matrix=matrix, w=req.w, initial_dist=req.initial_dist
        )
    else:
        df = replicator.pdNumerical(
            matrix=matrix, w=req.w, initial_dist=req.initial_dist
        )
        import numpy as np
        t_eval = np.linspace(0, 35, len(df))

    return {
        "t": list(map(float, t_eval)),
        "C": list(map(float, df["C"])),
        "D": list(map(float, df["D"])),
    }