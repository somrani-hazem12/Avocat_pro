import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../../components/sidebar/sidebar';
import { UtilisateurService } from '../../services/utilisateur';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterModule } from '@angular/router';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, FormsModule, SidebarComponent, MatCardModule, MatIconModule, 
    MatButtonModule, MatSnackBarModule, RouterModule, MatDividerModule, 
    MatChipsModule, MatTableModule, MatFormFieldModule, MatInputModule, MatSelectModule
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class Dashboard implements OnInit {
  nom = localStorage.getItem('nom') || '';
  role = localStorage.getItem('role') || '';
  loading = true;

  // --- DONNÉES CLIENT ---
  mesAvocats: any[] = [];
  avocatsDisponibles: any[] = [];

  // --- DONNÉES AVOCAT ---
  statsAvocat = { clients: 0, dossiers: 0, rdvAttente: 0 };

  // --- DONNÉES ADMIN ---
  statsAdmin = { total: 0, avocats: 0, clients: 0, enAttente: 0 };
  allUsers: any[] = [];
  usersEnAttente: any[] = [];
  showAdminForm = false;
  editModeAdmin = false;
  selectedUserId: number | null = null;
  adminForm = { nom: '', email: '', telephone: '', adresse: '', role: 'CLIENT', motDePasse: '' };
  displayedColumnsAdmin: string[] = ['nom', 'email', 'coordonnees', 'role', 'actions'];

  constructor(private userService: UtilisateurService, private snackBar: MatSnackBar) {}

  ngOnInit() {
    this.chargerDonnees();
  }

  chargerDonnees() {
    this.loading = true;
    if (this.role === 'ADMINISTRATEUR') {
      this.chargerDashboardAdmin();
    } else if (this.role === 'AVOCAT') {
      this.chargerDashboardAvocat();
    } else if (this.role === 'CLIENT') {
      this.chargerDashboardClient();
    } else {
      this.loading = false;
    }
  }



  ouvrirFormAdmin(user?: any) {
    this.showAdminForm = true;
    if (user) {
      this.editModeAdmin = true;
      this.selectedUserId = user.id;
      this.adminForm = { ...user, motDePasse: '' };
    } else {
      this.editModeAdmin = false;
      this.adminForm = { nom: '', email: '', telephone: '', adresse: '', role: 'CLIENT', motDePasse: 'avocat123' };
    }
  }

  enregistrerUserAdmin() {
    if (this.editModeAdmin && this.selectedUserId) {
      // APPEL AU BACKEND POUR LA MODIFICATION (PUT)
      this.userService.updateUser(this.selectedUserId, this.adminForm).subscribe({
        next: () => {
          this.snackBar.open("Modifications enregistrées", "OK", { duration: 2000 });
          this.fermerFormAdmin();
        },
        error: (err) => this.snackBar.open("Erreur lors de la mise à jour", "Fermer")
      });
    } else {
      // APPEL POUR LA CRÉATION (POST)
      this.userService.createUser(this.adminForm).subscribe(() => {
        this.snackBar.open("Nouvel utilisateur créé", "OK");
        this.fermerFormAdmin();
      });
    }
  }
    fermerFormAdmin() {
    this.showAdminForm = false;
    this.chargerDashboardAdmin(); // Rafraîchit le tableau
  }



  // =============================================
  // LOGIQUE AVOCAT
  // =============================================
  chargerDashboardAvocat() {
    this.userService.getStatsCabinet().subscribe({
      next: (data: any) => {
        this.statsAvocat = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  // =============================================
  // LOGIQUE CLIENT
  // =============================================
  chargerDashboardClient() {
    this.userService.getAvocatsDisponibles().subscribe({
      next: (avocats) => {
        this.avocatsDisponibles = avocats;
        this.userService.getProfile().subscribe({
          next: (user: any) => {
            this.mesAvocats = user.mesAvocats || [];
            this.loading = false;
          },
          error: () => this.loading = false
        });
      },
      error: () => this.loading = false
    });
  }

  // Actions partagées
  estDejaSuivi(id: number): boolean { return this.mesAvocats.some(a => a.id === id); }
  selectionnerAvocat(id: number) { 
    this.userService.choisirAvocat(id).subscribe(() => {
      this.snackBar.open("Avocat ajouté", "OK");
      this.chargerDashboardClient();
    }); 
  }
  retirerAvocat(id: number) { 
    if (confirm("Retirer ?")) this.userService.retirerAvocat(id).subscribe(() => this.chargerDashboardClient()); 
  }




 supprimerUserAdmin(id: number) {
    if (confirm("Supprimer définitivement cet utilisateur et TOUTES ses données liées ?")) {
      this.userService.deleteUser(id).subscribe({
        next: () => {
          this.snackBar.open("Suppression réussie", "OK");
          this.chargerDashboardAdmin();
        },
        error: (err) => this.snackBar.open(err.error.message, "Erreur")
      });
    }
  }

  chargerDashboardAdmin() {
    // 1. Charger les statistiques
    this.userService.getStatsGlobales().subscribe(stats => this.statsAdmin = stats);
    
    // 2. Charger la liste des utilisateurs (C'est cette partie qui remplit le tableau)
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.allUsers = data;
        this.loading = false;
        console.log("Utilisateurs chargés pour l'admin :", this.allUsers.length);
      },
      error: (err) => {
        console.error("Erreur chargement users", err);
        this.loading = false;
      }
    });
  }

  
}