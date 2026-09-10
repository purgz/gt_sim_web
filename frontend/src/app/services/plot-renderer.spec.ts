import { TestBed } from '@angular/core/testing';

import { buildPlotData, PlotRenderer } from './plot-renderer';

describe('buildPlotData', () => {
  it('should return empty for null or missing trajectories', () => {
    expect(buildPlotData(null)).toEqual([]);
    expect(buildPlotData({})).toEqual([]);
    expect(buildPlotData({ avg_trajectory: [] })).toEqual([]);
    expect(buildPlotData({ avg_trajectory: [[], []] })).toEqual([]);
  });

  it('should build one trace per strategy over frame indices', () => {
    const result = {
      avg_trajectory: [
        [1, 0.9, 0.7],
        [0, 0.05, 0.2],
        [0, 0.05, 0.1],
        [0, 0, 0],
      ],
    };

    const traces = buildPlotData(result) as any[];

    expect(traces.length).toBe(4);
    expect(traces[0].x).toEqual([0, 1, 2]);
    expect(traces[0].y).toEqual([1, 0.9, 0.7]);
    expect(traces[3].y).toEqual([0, 0, 0]);
    for (let i = 0; i < 4; i++) {
      expect(traces[i].name).toBe(`Strategy ${i + 1}`);
      expect(traces[i].mode).toBe('lines');
    }
  });

  it('should cycle line colours for more strategies than colours', () => {
    const result = {
      avg_trajectory: Array.from({ length: 5 }, () => [1, 0]),
    };

    const traces = buildPlotData(result) as any[];

    expect(traces.length).toBe(5);
    expect(traces[0].line.color).toBe(traces[4].line.color);
  });
});

describe('PlotRenderer', () => {
  let service: PlotRenderer;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PlotRenderer);
  });

  it('should detect trajectory presence via hasTrajectory', () => {
    expect(service.hasTrajectory({ avg_trajectory: [[1, 0]] } as any)).toBe(true);
    expect(service.hasTrajectory({ avg_trajectory: [] } as any)).toBe(false);
    expect(service.hasTrajectory(null)).toBe(false);
  });

  it('should clear safely when plotly never loaded', () => {
    const div = document.createElement('div');
    expect(() => service.clear(div)).not.toThrow();
  });
});
