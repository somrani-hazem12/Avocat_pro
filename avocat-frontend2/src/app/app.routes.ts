import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';
import { RegisterComponent } from './pages/register/register';
import { Dashboard } from './pages/dashboard/dashboard'; 
import { ClientsComponent } from './pages/clients/clients';
import { DossiersComponent } from './pages/dossiers/dossiers';
import { FacturesComponent } from './pages/factures/factures';
import { RendezVousComponent } from './pages/rendez-vous/rendez-vous';
import { UtilisateursComponent } from './pages/utilisateurs/utilisateurs'; 
import { NotificationsComponent } from './pages/notifications/notifications';
import { Logs } from './pages/logs/logs';                   
import { authGuard } from './guards/auth-guard';
import { roleGuard } from './guards/role.guard';
import { RiskComponent } from './pages/risk/risk';
export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
{ path: 'clients', component: ClientsComponent, canActivate: [authGuard] },
{ path: 'risk', component: RiskComponent, canActivate: [authGuard] },
{ path: 'notifications', component: NotificationsComponent, canActivate: [authGuard] },
{ path: 'dossiers', component: DossiersComponent, canActivate: [authGuard] },
{ path: 'factures', component: FacturesComponent, canActivate: [authGuard] },
  { path: 'rendez-vous', component: RendezVousComponent, canActivate: [authGuard] },
  
  { 
    path: 'utilisateurs', 
    component: UtilisateursComponent, 
    canActivate: [authGuard, roleGuard], 
    data: { roles: ['ADMINISTRATEUR'] } 
  },
  { 
    path: 'logs', 
    component: Logs, 
    canActivate: [authGuard, roleGuard], 
    data: { roles: ['ADMINISTRATEUR'] } 
  },
  
  { path: '**', redirectTo: 'login' }
];