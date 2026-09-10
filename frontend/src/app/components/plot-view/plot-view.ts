import { Component, ElementRef, computed, effect, inject, input, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlotRenderer } from '../../services/plot-renderer';

@Component({
  selector: 'app-plot-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './plot-view.html',
  styleUrl: './plot-view.scss',
})
export class PlotView {

  result = input<any>(null);

  private renderer = inject(PlotRenderer);
  private plotDiv = viewChild<ElementRef<HTMLDivElement>>('plotDiv');

  hasTrajectory = computed(() => this.renderer.hasTrajectory(this.result()));

  constructor() {
    effect(() => {
      const div = this.plotDiv()?.nativeElement;
      const res = this.result();
      if (div && this.hasTrajectory()) {
        void this.renderer.render(div, res);
      }
    });
  }
}
