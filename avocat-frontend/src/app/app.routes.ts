import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Dashboard } from './pages/dashboard/dashboard';
import { Clients } from './pages/clients/clients';
import { Dossiers } from './pages/dossiers/dossiers';
import { Factures } from './pages/factures/factures';
import { RendezVous } from './pages/rendez-vous/rendez-vous';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'clients', component: Clients, canActivate: [authGuard] },
  { path: 'dossiers', component: Dossiers, canActivate: [authGuard] },
  { path: 'factures', component: Factures, canActivate: [authGuard] },
  { path: 'rendez-vous', component: RendezVous, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' }
];