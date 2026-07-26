from fastapi import APIRouter, Depends, Header, HTTPException
import numpy as np
import os
from models.schemas import (
    CriticalNRequest, DeltaHRangeRequest, FixedPointRequest
)
import aug_rps_wrapper as wrapper

router = APIRouter()

INTERNAL_TOKEN = os.getenv("SIM_INTERNAL_TOKEN", "dev-token")

def verify(x_internal_token: str = Header(...)):
    if x_internal_token != INTERNAL_TOKEN:
        raise HTTPException(status_code=403, detail="Forbidden")

@router.post("/critical-n/analytical")
def critical_n_analytical(req: CriticalNRequest, _=Depends(verify)):
    """
    Analytical critical N for Local update process (closed-form expressions).
    Returns N_crit_SD and N_crit_RPS. May be NaN if no crossing exists.
    """
    matrix = np.array(req.matrix)
    N_sd, N_rps = wrapper.compute_critical_N_analytical(matrix, req.w)
    return {
        "N_crit_SD": N_sd,
        "N_crit_RPS": N_rps,
        "valid_SD": not (np.isnan(N_sd) or N_sd < 0),
        "valid_RPS": not (np.isnan(N_rps) or N_rps < 0),
    }

@router.post("/critical-n/brentq")
def critical_n_brentq(req: CriticalNRequest, _=Depends(verify)):
    """
    Numerically find critical N for SD using Brent's method.
    Works for any process. Slower than analytical but more general.
    """
    matrix = np.array(req.matrix)
    try:
        N_crit = wrapper.compute_critical_N_sd_brentq(
            matrix=matrix,
            w=req.w,
            process=req.process,
            n_lo=req.n_lo,
            n_hi=req.n_hi,
        )
        return {"N_crit_SD": N_crit, "found": True}
    except ValueError as e:
        return {"N_crit_SD": None, "found": False, "reason": str(e)}

@router.post("/delta-h-range")
def delta_h_range(req: DeltaHRangeRequest, _=Depends(verify)):
    """
    Compute <ΔH>·N² for SD, RPS, and + across a range of N values.
    This is the main drift analysis plot.
    """
    matrix = np.array(req.matrix)
    n_range = np.linspace(req.n_min, req.n_max, req.n_points)

    sd_vals, rps_vals, plus_vals = wrapper.compute_delta_H_range(
        matrix=matrix,
        w=req.w,
        n_range=n_range,
        process=req.process,
    )

    # Also compute analytical critical N for Local (if requested process is Local)
    analytical = {}
    if req.process == "Local":
        N_sd, N_rps = wrapper.compute_critical_N_analytical(matrix, req.w)
        analytical = {"N_crit_SD": N_sd, "N_crit_RPS": N_rps}

    return {
        "n_range": list(map(float, n_range)),
        "delta_H_SD": list(map(float, sd_vals)),
        "delta_H_RPS": list(map(float, rps_vals)),
        "delta_H_plus": list(map(float, plus_vals)),
        "analytical_critical_N": analytical,
    }

@router.post("/fixed-point")
def fixed_point(req: FixedPointRequest, _=Depends(verify)):
    """
    Find the interior fixed point of the replicator (Moran process).
    Returns [x*, y*, z*, q*].
    """
    matrix = np.array(req.matrix)
    fp = wrapper.compute_fixed_point(matrix, req.w)
    if fp is None:
        return {"found": False, "fixed_point": None}
    return {
        "found": True,
        "fixed_point": list(map(float, fp)),
        "labels": ["R", "P", "S", "L"],
    }