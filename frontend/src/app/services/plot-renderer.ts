import { Injectable } from '@angular/core';

export interface TraceSpec {
  x: number[];
  y: number[];
  mode: 'lines';
  name: string;
  line: { color: string };
}

type PlotlyModule = {
  newPlot: (div: HTMLElement, data: any, layout?: any, config?: any) => Promise<any>;
  purge: (div: HTMLElement) => void;
};

type PlotlyResult = {
  avg_trajectory?: number[][] | null;
  n_frames?: number;
};

const LINE_COLORS = [
  '#4a90e2',
  '#27ae60',
  '#e74c3c',
  '#8e44ad',
];

export function buildPlotData(result: PlotlyResult | null): TraceSpec[] {
  const traj = result?.avg_trajectory;
  if (!Array.isArray(traj) || traj.length === 0) {
    return [];
  }

  const nFrames = Array.isArray(traj[0]) ? traj[0].length : 0;
  if (nFrames === 0) {
    return [];
  }

  return traj.map((row, i) => ({
    x: Array.from({ length: nFrames }, (_, t) => t),
    y: row,
    mode: 'lines' as const,
    name: `Strategy ${i + 1}`,
    line: { color: LINE_COLORS[i % LINE_COLORS.length] },
  }));
}

@Injectable({ providedIn: 'root' })
export class PlotRenderer {

  private plotly: PlotlyModule | null = null;
  private loading: Promise<PlotlyModule> | null = null;

  private load(): Promise<PlotlyModule> {
    if (this.plotly) {
      return Promise.resolve(this.plotly);
    }
    if (!this.loading) {
      this.loading = import('plotly.js-dist-min').then(m => {
        const mod = (m.default ?? m) as PlotlyModule;
        this.plotly = mod;
        return mod;
      });
    }
    return this.loading;
  }

  hasTrajectory(result: PlotlyResult | null): boolean {
    return buildPlotData(result).length > 0;
  }

  render(div: HTMLElement, result: PlotlyResult | null): Promise<void> {
    const traces = buildPlotData(result);
    if (traces.length === 0) {
      return Promise.resolve();
    }

    const layout = {
      yaxis: { range: [0, 1], title: { text: 'Population fraction' } },
      xaxis: { title: { text: 'Simulation frame' } },
      hovermode: 'x unified',
      margin: { t: 10, r: 20, b: 40, l: 50 },
      legend: { orientation: 'h' },
    };
    const config = {
      displaylogo: false,
      responsive: true,
    };

    return this.load().then(p =>
      p.newPlot(div, traces, layout, config).then(() => undefined));
  }

  clear(div: HTMLElement) {
    if (this.plotly) {
      this.plotly.purge(div);
    }
  }
}
