from fastapi import APIRouter, Depends, Header, HTTPException
import numpy as np
import os
import simulation
from models.schemas import SimRequest

router = APIRouter()

INTERNAL_TOKEN = os.getenv("SIM_INTERNAL_TOKEN", "dev-token")

def verify(x_internal_token: str = Header(...)):
    if x_internal_token != INTERNAL_TOKEN:
        raise HTTPException(status_code=403, detail="Forbidden")

_SIM_FUNCS = {
    "Moran": simulation.moran_batch_sim,
    "Local": simulation.local_batch_sim,
    "Fermi": simulation.fermi_batch_sim,
}

@router.post("/run")
def run_simulation(req: SimRequest, _=Depends(verify)):
    matrix = np.array(req.matrix)
    sim_func = _SIM_FUNCS[req.process]

    kwargs = dict(
        pop_size=req.pop_size,
        iterations=req.iterations,
        simulations=req.simulations,
        w=req.w,
        matrix=matrix,
        traj=req.traj,
        point_cloud=req.point_cloud,
        initial_rand=req.initial_rand,
    )
    if req.initial_dist is not None:
        kwargs["initial_dist"] = np.array(req.initial_dist)

    delta_H, delta_H_RPS, avg_traj, all_traj = sim_func(**kwargs)

    # avg_traj shape: (n_strategies, n_frames)
    avg_traj_list = [list(map(float, row)) for row in avg_traj]

    # all_traj can be large — only include if point_cloud=False
    all_traj_list = None
    if req.point_cloud and all_traj is not None:
        # shape: (n_sims, n_strategies, n_frames)
        all_traj_list = all_traj.tolist()

    return {
        "delta_H": float(delta_H),
        "delta_H_RPS": float(delta_H_RPS),
        "avg_trajectory": avg_traj_list,
        "all_trajectories": all_traj_list,
        "n_strategies": len(avg_traj_list),
        "n_frames": len(avg_traj_list[0]) if avg_traj_list else 0,
    }