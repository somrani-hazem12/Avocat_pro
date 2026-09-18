import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterModule,
    MatCardModule, MatInputModule, MatButtonModule,
    MatIconModule, MatFormFieldModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {
  email = '';
  motDePasse = '';
  erreur = '';
  hidePassword = true;

  constructor(private authService: AuthService, private router: Router) {}

  login() {
    this.authService.login(this.email, this.motDePasse).subscribe({
      next: () => {
        const role = this.authService.getRole();
        
        // Redirection dynamique selon le rôle
        if (role === 'ADMINISTRATEUR') {
          this.router.navigate(['/utilisateurs']); // L'admin va gérer les utilisateurs
        } else if (role === 'CLIENT') {
          this.router.navigate(['/dossiers']); // Le client voit ses dossiers directement
        } else {
          this.router.navigate(['/dashboard']); // Avocat et Assistant vont sur le dashboard
        }
      },
      error: (err) => {
        // Affiche le message renvoyé par le backend (ex: "Compte EN_ATTENTE")
        this.erreur = typeof err.error === 'string' ? err.error : 'Email ou mot de passe incorrect !';
      }
    });
  }
}