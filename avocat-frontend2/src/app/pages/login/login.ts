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
    console.log('=== LOGIN COMPONENT ===');
    console.log('Email saisi:', this.email);
    console.log('Mot de passe saisi:', this.motDePasse);
    
    this.authService.login(this.email, this.motDePasse).subscribe({
      next: (response) => {
        console.log('Réponse reçue dans component:', response);
        
        const role = this.authService.getRole();
        console.log('Rôle récupéré:', role);
        console.log('Token récupéré:', this.authService.getToken());
        
        // Redirection dynamique selon le rôle
        if (role === 'ADMINISTRATEUR') {
          console.log('Redirection vers /utilisateurs');
          this.router.navigate(['/utilisateurs']);
        } else if (role === 'CLIENT') {
          console.log('Redirection vers /dossiers');
          this.router.navigate(['/dossiers']);
        } else {
          console.log('Redirection vers /dashboard');
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        console.error('Erreur complète:', err);
        console.error('Message erreur:', err.error);
        
        // Affiche le message renvoyé par le backend (ex: "Compte EN_ATTENTE")
        this.erreur = typeof err.error === 'string' ? err.error : 'Email ou mot de passe incorrect !';
      }
    });
  }
}