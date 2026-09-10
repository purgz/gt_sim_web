import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlotView } from './plot-view';
import { PlotRenderer } from '../../services/plot-renderer';

describe('PlotView', () => {
  let component: PlotView;
  let fixture: ComponentFixture<PlotView>;
  let renderSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlotView],
    }).compileComponents();

    const renderer = TestBed.inject(PlotRenderer);
    renderSpy = vi.spyOn(renderer, 'render')
      .mockResolvedValue(undefined) as unknown as typeof renderSpy;
    renderSpy.mockImplementation(async () => undefined);

    fixture = TestBed.createComponent(PlotView);
    component = fixture.componentInstance;
  });

  it('should create with no result', () => {
    expect(component).toBeTruthy();
    expect(component.hasTrajectory()).toBe(false);
  });

  it('should report no trajectory when avg_trajectory missing', () => {
    fixture.componentRef.setInput('result', { delta_H: 1.5, n_frames: 0 });
    expect(component.hasTrajectory()).toBe(false);
  });

  it('should not render when no trajectory data', async () => {
    fixture.componentRef.setInput('result', { avg_trajectory: [] });
    await fixture.whenStable();
    expect(renderSpy).not.toHaveBeenCalled();
  });

  it('should call renderer when trajectory data present', async () => {
    fixture.componentRef.setInput('result', {
      avg_trajectory: [
        [1, 0.9, 0.7],
        [0, 0.05, 0.2],
        [0, 0.05, 0.1],
        [0, 0, 0],
      ],
      n_frames: 3,
      n_strategies: 4,
    });
    await fixture.whenStable();
    expect(renderSpy).toHaveBeenCalled();
    const traces = (renderSpy as any).mock.calls[0][1];
    expect(traces.avg_trajectory.length).toBe(4);
  });
});
