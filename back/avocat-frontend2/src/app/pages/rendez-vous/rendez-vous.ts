import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../../components/sidebar/sidebar';
import { RendezVousService } from '../../services/rendez-vous';
import { UtilisateurService } from '../../services/utilisateur';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-rendez-vous',
  standalone: true,
  imports: [
    CommonModule, FormsModule, SidebarComponent, MatCardModule, 
    MatFormFieldModule, MatInputModule, MatSelectModule, 
    MatButtonModule, MatTableModule, MatIconModule, MatSnackBarModule
  ],
  templateUrl: './rendez-vous.html',
  styleUrls: ['./rendez-vous.css']
})
export class RendezVousComponent implements OnInit {
  role = localStorage.getItem('role');
  rdvs: any[] = [];
  mesMaitres: any[] = [];
  selectedAvocatId: number | null = null;
  newRdv = { description: '' };
  displayedColumns: string[] = ['motif', 'date', 'etat', 'actions'];

  // --- NOUVELLES VARIABLES POUR FIXER LA DATE ---
  showFixDateForm = false;
  rdvToFixId: number | null = null;
  dateChoisie: string = ''; // Format: 2026-04-10T14:30

  constructor(
    private rdvService: RendezVousService,
    private userService: UtilisateurService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.chargerDonnees();
  }

  chargerDonnees() {
    this.chargerMesRdv();
    if (this.role === 'CLIENT') {
      this.userService.getProfile().subscribe(user => {
        this.mesMaitres = user.mesAvocats || [];
      });
    }
  }

  chargerMesRdv() {
    this.rdvService.getAll().subscribe(data => this.rdvs = data);
  }

  prendreRdv() {
    if (!this.selectedAvocatId || !this.newRdv.description) return;
    const payload = {
      motif: this.newRdv.description,
      utilisateur: { id: this.selectedAvocatId }
    };
    this.rdvService.creer(payload).subscribe(() => {
      this.snackBar.open("Demande envoyée !", "OK", { duration: 3000 });
      this.newRdv.description = '';
      this.chargerMesRdv();
    });
  }

  // --- LOGIQUE POUR LE CALENDRIER ---
  
  // 1. Ouvre le petit formulaire
  ouvrirFormDate(id: number) {
    this.rdvToFixId = id;
    this.showFixDateForm = true;
  }

  // 2. Annule
  annulerFixation() {
    this.showFixDateForm = false;
    this.rdvToFixId = null;
    this.dateChoisie = '';
  }

  // 3. Valide et envoie au Backend
  validerFixation() {
    if (!this.dateChoisie || !this.rdvToFixId) {
      this.snackBar.open("Veuillez choisir une date valide", "OK");
      return;
    }

    this.rdvService.confirmer(this.rdvToFixId, this.dateChoisie).subscribe({
      next: () => {
        this.snackBar.open("Rendez-vous confirmé !", "OK");
        this.annulerFixation();
        this.chargerMesRdv();
      },
      error: () => this.snackBar.open("Erreur lors de la confirmation", "Fermer")
    });
  }
}