import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RunSim } from './run-sim';
import { Sim } from '../../services/sim';

const DEFAULT_RPS_MATRIX = [
  [0, -1, 1, 0],
  [1, 0, -1, 0],
  [-1, 1, 0, 0],
  [0, 0, 0, 0],
];

describe('RunSim', () => {
  let component: RunSim;
  let fixture: ComponentFixture<RunSim>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RunSim],
    }).compileComponents();

    fixture = TestBed.createComponent(RunSim);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should default to a zero-sum RPS matrix with neutral 4th strategy', () => {
    expect(component.buildRequest().matrix).toEqual(DEFAULT_RPS_MATRIX);
  });

  it('should restore the default matrix on reset', () => {
    component.setCell(0, 0, '9');
    component.resetMatrix();
    expect(component.matrix).toEqual(DEFAULT_RPS_MATRIX);
  });

  it('should build correctly typed request', () => {
    component.process.set('Fermi');
    component.setCell(0, 0, '2');
    component.setCell(1, 1, '1.5');

    const req = component.buildRequest();

    expect(req.process).toBe('Fermi');
    expect(req.matrix).toEqual([
      [2, -1, 1, 0],
      [1, 1.5, -1, 0],
      [-1, 1, 0, 0],
      [0, 0, 0, 0],
    ]);
    expect(req.pop_size).toBe(100);
    expect(req.iterations).toBe(10000);
    expect(req.simulations).toBe(50);
    expect(req.w).toBe(0.45);
    expect(req.traj).toBe(true);
    expect(req.point_cloud).toBe(false);
    expect(req.initial_rand).toBe(false);
  });

  it('should omit initial_dist unless enabled', () => {
    const req = component.buildRequest();

    expect('initial_dist' in req).toBe(false);
  });

  it('should include parsed initial_dist when enabled', () => {
    component.toggleUseInitialDist(true);
    component.setInitialDist('0.6, 0.1, 0.2, 0.1');

    const req = component.buildRequest();

    expect(component.distComplete()).toBe(true);
    expect('initial_dist' in req).toBe(true);
    expect(req.initial_dist).toEqual([0.6, 0.1, 0.2, 0.1]);
  });

  it('should make initial_dist and initial_rand mutually exclusive', () => {
    component.toggleUseInitialDist(true);
    expect(component.initialRand()).toBe(false);

    component.setInitialRand(true);
    expect(component.useInitialDist()).toBe(false);
    expect('initial_dist' in component.buildRequest()).toBe(false);
  });

  it('should block run when matrix or dist invalid, then call sim service', () => {
    const sim = TestBed.inject(Sim);
    const spy = vi.spyOn(sim, 'runSimulation');
    spy.mockReturnValue({ subscribe: vi.fn() } as any);

    component.setCell(2, 3, 'abc');
    expect(component.matrixComplete()).toBe(false);

    component.run();
    expect(spy).not.toHaveBeenCalled();

    component.setCell(2, 3, '1');
    component.toggleUseInitialDist(true);
    component.setInitialDist('0.5, 0.5');
    expect(component.distComplete()).toBe(false);

    component.run();
    expect(spy).not.toHaveBeenCalled();

    component.setInitialDist('0.25, 0.25, 0.25, 0.25');
    expect(component.distComplete()).toBe(true);

    component.run();
    expect(spy).toHaveBeenCalledTimes(1);
    expect(spy.mock.calls[0][0].process).toBe('Moran');
    expect(component.running()).toBe(true);
  });
});
