import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Auth } from '../../services/auth';
import { Sim } from '../../services/sim';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {

  private auth = inject(Auth);
  private sim = inject(Sim);

  user$ = this.auth.currentUser$;

  health = signal<any>(null);
  healthLoading = signal(false);

  savedSims = signal<any[]>([]);
  savedLoading = signal(false);

  selectedSim = signal<any>(null);
  selectedLoading = signal(false);


  ngOnInit() {
    this.loadSaved();
  }


  checkHealth() {
    this.healthLoading.set(true);

    this.sim.getSimServiceHealth().subscribe({
      next: res => {
        this.health.set(res);
        this.healthLoading.set(false);
      },
      error: err => {
        console.error('Health check failed:', err);

        this.health.set({
          error: 'Could not reach service'
        });

        this.healthLoading.set(false);
      }
    });
  }


  loadSaved() {
    this.savedLoading.set(true);

    this.sim.getSavedSimulations().subscribe({
      next: res => {
        this.savedSims.set(res);
        this.savedLoading.set(false);
      },
      error: err => {
        console.error('Loading saved simulations failed:', err);

        this.savedSims.set([]);
        this.savedLoading.set(false);
      }
    });
  }


  viewSim(id: string) {
    this.selectedLoading.set(true);
    this.selectedSim.set(null);

    this.sim.getSavedSimulation(id).subscribe({
      next: res => {
        this.selectedSim.set(res);
        this.selectedLoading.set(false);
      },
      error: err => {
        console.error('Loading simulation failed:', err);

        this.selectedSim.set({
          error: 'Could not load simulation'
        });

        this.selectedLoading.set(false);
      }
    });
  }


  logout() {
    this.auth.logout();
  }
}

