import { Component, inject, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Sim } from '../../services/sim';

const PROCESSES = ['Moran', 'Local', 'Fermi'] as const;
const MATRIX_SIZE = 4;
const STRATEGY_COUNT = 4;

const DEFAULT_MATRIX = [
  [0, -1, 1, 0],
  [1, 0, -1, 0],
  [-1, 1, 0, 0],
  [0, 0, 0, 0],
];

@Component({
  selector: 'app-run-sim',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './run-sim.html',
  styleUrl: './run-sim.scss',
})
export class RunSim {

  private sim = inject(Sim);

  processes = PROCESSES;

  process = signal<string>('Moran');
  popSize = '100';
  iterations = '10000';
  simulations = '50';
  w = '0.45';

  traj = signal(true);
  pointCloud = signal(false);
  initialRand = signal(false);

  useInitialDist = signal(false);
  initialDist = signal('');
  distComplete = signal(true);

  matrix: number[][] = DEFAULT_MATRIX.map(row => [...row]);

  matrixComplete = signal(true);

  running = signal(false);
  result = signal<any>(null);
  error = signal('');

  resultReady = output<any>();

  setCell(row: number, col: number, value: string) {
    this.matrix[row][col] = Number(value);
    this.matrixComplete.set(
      this.matrix.every(r => r.every(c => Number.isFinite(c))));
  }

  resetMatrix() {
    this.matrix = DEFAULT_MATRIX.map(row => [...row]);
    this.matrixComplete.set(true);
  }

  toggleUseInitialDist(on: boolean) {
    this.useInitialDist.set(on);
    if (on) {
      this.initialRand.set(false);
    } else {
      this.initialDist.set('');
      this.distComplete.set(true);
    }
  }

  setInitialRand(on: boolean) {
    this.initialRand.set(on);
    if (on) {
      this.useInitialDist.set(false);
      this.initialDist.set('');
      this.distComplete.set(true);
    }
  }

  setInitialDist(value: string) {
    this.initialDist.set(value);
    const values = value.split(',').map(v => Number(v.trim()));
    this.distComplete.set(
      values.length === STRATEGY_COUNT && values.every(v => Number.isFinite(v)));
  }

  parsedInitialDist() {
    return this.initialDist().split(',').map(v => Number(v.trim()));
  }

  buildRequest() {
    const distActive = this.useInitialDist() && this.distComplete();
    return {
      process: this.process(),
      matrix: this.matrix.map(row => row.map(cell => Number(cell))),
      pop_size: Number(this.popSize),
      iterations: Number(this.iterations),
      simulations: Number(this.simulations),
      w: Number(this.w),
      traj: this.traj(),
      point_cloud: this.pointCloud(),
      initial_rand: this.initialRand(),
      ...(distActive ? { initial_dist: this.parsedInitialDist() } : {}),
    };
  }

  run() {
    if (this.running() || !this.matrixComplete() || !this.distComplete()) {
      return;
    }

    this.running.set(true);
    this.error.set('');
    this.result.set(null);

    this.sim.runSimulation(this.buildRequest()).subscribe({
      next: res => {
        this.result.set(res);
        this.running.set(false);
        this.resultReady.emit(res);
      },
      error: err => {
        this.error.set(err.status ? `Run failed: HTTP ${err.status}` : 'Run failed: service unreachable');
        this.running.set(false);
      },
    });
  }
}
