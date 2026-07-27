import { inject, Service } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';

@Service()
export class Sim {
    private base = environment.apiUrl;

    private http = inject(HttpClient);

    getSimServiceHealth() {
        return this.http.get<any>(`${this.base}/sim/health`);
    }

    getSavedSimulations() {
        return this.http.get<any[]>(`${this.base}/sim/saved`);
    }

    getSavedSimulation(id: string) {
        return this.http.get<any>(`${this.base}/sim/saved/${id}`);
    }

    runSimulation(params: any) {
        return this.http.post<any>(`${this.base}/sim/run`, params);
    }
}