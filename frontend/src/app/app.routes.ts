import { Routes } from '@angular/router';
import { inject } from '@angular/core';
import { Auth } from './services/auth';
import { Router } from '@angular/router';

import { Login } from './components/login/login';
import { Dashboard } from './components/dashboard/dashboard';

export const routes: Routes = [
  { path: 'login', component: Login },
  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [() => {
      const auth = inject(Auth);
      const router = inject(Router);
      if (!auth.isLoggedIn()) {
        router.navigate(['/login']);
        return false;
      }
      return true;
    }]
  },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: '/dashboard' }
];