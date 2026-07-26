"""
Wrapper utilities for replicator module functions that require
sympy symbol manipulation or have hardcoded values in the source.
"""
import numpy as np
import sympy as sp
from sympy.utilities.lambdify import lambdify
from scipy.optimize import brentq
from scipy.integrate import nquad, tplquad

from replicator.aug_rps import (
    a, b, c, gamma, beta,
    x, y, z, q,
    payoffR, payoffP, payoffS, payoffL,
    w_sym, delta_pi as delta_pi_sym,
    transition_probs,
    local_reproductive_func,
    moran_reproductive_func,
    fermi_reproductive_func,
    delta_H_SD, delta_H_4, delta_H_RPS,
    delta_h_SD_LOCAL_CRIT_N, delta_H_RPS_LOCAL_CRIT_N,
    numerical_H_value,
)


def matrix_to_config(matrix: np.ndarray) -> dict:
    a_val = float(matrix[0, 0])
    c_val = float(matrix[0, 1])
    b_val = float(matrix[0, 2])
    gamma_val = float(matrix[0, 3])
    beta_val = float(matrix[3, 0]) - a_val
    return {a: a_val, b: b_val, c: c_val, gamma: gamma_val, beta: beta_val}


def compute_critical_N_analytical(matrix: np.ndarray, w: float) -> tuple:
    config = matrix_to_config(matrix)
    d_pi = float(matrix.max() - matrix.min())

    N_sd_sym = delta_h_SD_LOCAL_CRIT_N().subs(config).subs(delta_pi_sym, d_pi).subs(w_sym, w)
    N_rps_sym = delta_H_RPS_LOCAL_CRIT_N().subs(config).subs(delta_pi_sym, d_pi).subs(w_sym, w)

    try:
        N_sd = float(N_sd_sym)
    except Exception:
        N_sd = float("nan")
    try:
        N_rps = float(N_rps_sym)
    except Exception:
        N_rps = float("nan")

    return N_sd, N_rps


_PROC_FUNC_MAP = {
    "Local": local_reproductive_func,
    "Moran": moran_reproductive_func,
    "Fermi": fermi_reproductive_func,
}


def compute_delta_H_range(
    matrix: np.ndarray,
    w: float,
    n_range: np.ndarray,
    process: str = "Local",
    progress_callback=None,
) -> tuple:
    config = matrix_to_config(matrix)
    d_pi = float(matrix.max() - matrix.min())

    reproductive_func = _PROC_FUNC_MAP.get(process, local_reproductive_func)
    transitions = transition_probs(
        reproductive_func, [payoffR, payoffP, payoffS, payoffL]
    )

    sd_vals, rps_vals, plus_vals = [], [], []

    for i, n in enumerate(n_range):
        if progress_callback:
            progress_callback(i, len(n_range), float(n))
        res_sd, res_rps, res_4 = numerical_H_value(
            transitions,
            N=float(n),
            w=w,
            custom_config=config,
            delta_pi_custom=d_pi,
        )
        sd_vals.append(res_sd * n * n)
        rps_vals.append(res_rps * n * n)
        plus_vals.append(res_4 * n * n)

    return np.array(sd_vals), np.array(rps_vals), np.array(plus_vals)


def compute_critical_N_sd_brentq(
    matrix: np.ndarray,
    w: float,
    process: str,
    n_lo: float = 5.0,
    n_hi: float = 5000.0,
) -> float:
    config = matrix_to_config(matrix)
    d_pi = float(matrix.max() - matrix.min())

    reproductive_func = _PROC_FUNC_MAP[process]
    transitions = transition_probs(
        reproductive_func, [payoffR, payoffP, payoffS, payoffL]
    )

    transitions_sub = {
        key: val.subs(config).subs(w_sym, w).subs(delta_pi_sym, d_pi)
        for key, val in transitions.items()
    }

    N_sym = sp.Symbol("N")
    expression = delta_H_SD(transitions_sub)

    f_full = lambdify((N_sym, x, y, z), expression, "numpy")

    def h_of_N(N_val: float) -> float:
        Nv = float(N_val)
        def integrand(zz, yy, xx):
            return f_full(Nv, xx, yy, zz)
        res, _ = tplquad(
            integrand,
            0, 1,
            lambda xx: 0,
            lambda xx: 1.0 - xx,
            lambda xx, yy: 0,
            lambda xx, yy: 1.0 - xx - yy,
        )
        return res

    f_lo = h_of_N(n_lo)
    f_hi = h_of_N(n_hi)

    if f_lo * f_hi > 0:
        raise ValueError(
            f"No sign change for {process} SD in N∈[{n_lo:.0f}, {n_hi:.0f}]: "
            f"ΔH({n_lo:.0f})={f_lo:.4e}, ΔH({n_hi:.0f})={f_hi:.4e}."
        )

    return float(brentq(h_of_N, n_lo, n_hi, xtol=1.0))


def compute_fixed_point(matrix: np.ndarray, w: float) -> np.ndarray:
    import replicator
    try:
        return replicator.find_fixed_point_a_x(matrix, w=w)
    except Exception:
        return None