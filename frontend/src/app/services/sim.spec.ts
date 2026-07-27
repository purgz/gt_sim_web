import { TestBed } from '@angular/core/testing';

import { Sim } from './sim';

describe('Sim', () => {
  let service: Sim;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Sim);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
