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
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-rendez-vous',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatSnackBarModule],
  templateUrl: './rendez-vous.html',
  styleUrls: ['./rendez-vous.css']
})
export class RendezVousComponent implements OnInit {
  role = localStorage.getItem('role');
  nomUtilisateur = localStorage.getItem('nom');
  rdvs: any[] = [];
  mesMaitres: any[] = [];

  // Calendrier
  joursDuMois: (Date | null)[] = [];
  moisAffiche = new Date().getMonth();
  anneeAffiche = new Date().getFullYear();
  nomsJours = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

  // Modals et Formulaires
  showRequestForm = false;
  showFixDateForm = false;
  selectedAvocatId: number | null = null;
  newRdv = { description: '' };
  rdvToFix: any = null;
  dateChoisie: string = '';
  heureChoisie: string = '10:00';

  constructor(private rdvService: RendezVousService, private userService: UtilisateurService, private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.chargerDonnees();
    this.genererCalendrier();
  }

  chargerDonnees() {
    this.rdvService.getAll().subscribe(data => this.rdvs = data);
    if (this.role === 'CLIENT') {
      this.userService.getProfile().subscribe(user => this.mesMaitres = user.mesAvocats || []);
    }
  }

  // --- LOGIQUE CALENDRIER ---
  genererCalendrier() {
    const premier = new Date(this.anneeAffiche, this.moisAffiche, 1);
    const dernier = new Date(this.anneeAffiche, this.moisAffiche + 1, 0);
    this.joursDuMois = [];
    let debut = premier.getDay();
    debut = debut === 0 ? 6 : debut - 1; 
    for (let i = 0; i < debut; i++) this.joursDuMois.push(null);
    for (let i = 1; i <= dernier.getDate(); i++) this.joursDuMois.push(new Date(this.anneeAffiche, this.moisAffiche, i));
  }

  getRdvsDuJour(date: Date | null): any[] {
    if (!date) return [];
    const dStr = date.getFullYear() + "-" + String(date.getMonth() + 1).padStart(2, '0') + "-" + String(date.getDate()).padStart(2, '0');
    return this.rdvs.filter(r => r.dateHeure && r.dateHeure.startsWith(dStr));
  }

  getRdvsEnAttente(): any[] {
    return this.rdvs.filter(r => r.statut === 'DEMANDE');
  }

  // --- ACTIONS ---
  ouvrirFixation(rdv: any) {
    this.rdvToFix = rdv;
    this.showFixDateForm = true;
  }

  validerFixation() {
    if (!this.dateChoisie || !this.rdvToFix) return;
    const payload = `${this.dateChoisie}T${this.heureChoisie}`;
    this.rdvService.confirmer(this.rdvToFix.id, payload).subscribe(() => {
      this.snackBar.open("Rendez-vous confirmé et placé dans l'agenda", "OK");
      this.showFixDateForm = false;
      this.chargerDonnees();
    });
  }

  prendreRdv() {
    const payload = { motif: this.newRdv.description, utilisateur: { id: this.selectedAvocatId } };
    this.rdvService.creer(payload).subscribe(() => {
      this.snackBar.open("Demande envoyée !", "OK");
      this.showRequestForm = false;
      this.chargerDonnees();
    });
  }

  changerMois(dir: number) {
    this.moisAffiche += dir;
    if (this.moisAffiche > 11) { this.moisAffiche = 0; this.anneeAffiche++; }
    else if (this.moisAffiche < 0) { this.moisAffiche = 11; this.anneeAffiche--; }
    this.genererCalendrier();
  }

  get NomMois() { return new Intl.DateTimeFormat('fr-FR', { month: 'long', year: 'numeric' }).format(new Date(this.anneeAffiche, this.moisAffiche)); }
}