import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  // Les rôles autorisés passés dans les routes (ex: ['ADMINISTRATEUR'])
  const expectedRoles = route.data['roles'] as Array<string>;
  const currentRole = authService.getRole();
  
  if (currentRole && expectedRoles.includes(currentRole)) {
    return true; // L'utilisateur a le bon rôle, on le laisse passer
  }
  
  // S'il n'a pas le droit, on le renvoie vers la page appropriée
  if (currentRole === 'ADMINISTRATEUR') {
    router.navigate(['/utilisateurs']);
  } else {
    router.navigate(['/dashboard']);
  }
  
  return false;
};