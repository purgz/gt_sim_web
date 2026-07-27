import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../environments/environment';


export interface AuthResponse {
  token: string;
  email: string;
  roles: string[];
}

@Service()
export class Auth {
    private base = environment.apiUrl;
    private tokenKey = 'jwt_token';

    currentUser$ = new BehaviorSubject<AuthResponse | null>(this.getStoredUser());

    private http = inject(HttpClient);
    private router = inject(Router);

    login(email: string, password: string) {
        console.log("Environment API URL:", this.base); // Debugging line to check the base URL
        console.log(environment.production);

        
        return this.http.post<AuthResponse>(`${this.base}/auth/login`, { email, password })
        .pipe(tap(res => {
            localStorage.setItem(this.tokenKey, res.token);
            localStorage.setItem('user', JSON.stringify(res));
            this.currentUser$.next(res);
        }));
    }

    logout() {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem('user');
        this.currentUser$.next(null);
        this.router.navigate(['/login']);
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    isLoggedIn(): boolean {
        return !!this.getToken();
    }

    isAdmin(): boolean {
        return this.currentUser$.value?.roles?.includes('ROLE_ADMIN') ?? false;
    }

    private getStoredUser(): AuthResponse | null {
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    }
}
