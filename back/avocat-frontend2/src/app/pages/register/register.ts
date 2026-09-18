import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterModule,
    MatCardModule, MatInputModule, MatButtonModule,
    MatIconModule, MatFormFieldModule, MatSelectModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  erreur = '';
  succes = '';
  hidePassword = true;
  loading = false;

  constructor(
    private fb: FormBuilder, 
    private authService: AuthService, 
    private router: Router
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      nom: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [Validators.required, Validators.minLength(6)]],
      role: ['CLIENT', Validators.required],
      // Regex pour téléphone tunisien (8 chiffres, commence par 2, 4, 5 ou 9)
      telephone: ['', [Validators.required, Validators.pattern('^[2459][0-9]{7}$')]],
      adresse: ['', Validators.required],
      specialite: ['']
    });

    // Gestion dynamique de la spécialité obligatoire si Avocat
    this.registerForm.get('role')?.valueChanges.subscribe(role => {
      const specControl = this.registerForm.get('specialite');
      if (role === 'AVOCAT') {
        specControl?.setValidators([Validators.required]);
      } else {
        specControl?.clearValidators();
      }
      specControl?.updateValueAndValidity();
    });
  }

  register() {
    if (this.registerForm.invalid) {
      this.erreur = "Veuillez remplir correctement tous les champs.";
      return;
    }

    this.loading = true;
    this.erreur = '';
    
    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        this.loading = false;
        this.succes = "Inscription réussie ! Votre compte est en attente de validation par l'administrateur.";
        setTimeout(() => this.router.navigate(['/login']), 4000);
      },
      error: (err) => {
        this.loading = false;
        this.erreur = err.error || "Une erreur est survenue lors de l'inscription.";
      }
    });
  }

  goToLogin() { this.router.navigate(['/login']); }
}