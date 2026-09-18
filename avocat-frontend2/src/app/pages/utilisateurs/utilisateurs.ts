import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin';
import { SidebarComponent } from '../../components/sidebar/sidebar';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-utilisateurs',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    SidebarComponent,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './utilisateurs.html',
  styleUrls: ['./utilisateurs.css']
})
export class UtilisateursComponent implements OnInit {

  utilisateursEnAttente: any[] = [];
  tousLesUtilisateurs: any[] = [];
  utilisateursFiltres: any[] = [];
  avocats: any[] = [];
  
  stats = { 
    total: 0, 
    avocats: 0, 
    clients: 0, 
    enAttente: 0, 
    actifs: 0, 
    bloques: 0 
  };
  
  colonnesAffichees: string[] = ['nom', 'email', 'role', 'date', 'actions'];
  searchTerm: string = '';
  filtreActif: string = 'tous';
  selectedUser: any = null;

  constructor(
    private adminService: AdminService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.chargerDonnees();
  }

  chargerDonnees(): void {
    // Charger tous les utilisateurs
    this.adminService.getAllUtilisateurs().subscribe({
      next: (data) => {
        this.tousLesUtilisateurs = data;
        this.calculerStats();
        this.filtrerUtilisateurs();
      },
      error: (err) => console.error('Erreur chargement:', err)
    });
    
    // Charger les inscriptions en attente
    this.adminService.getEnAttente().subscribe({
      next: (data) => this.utilisateursEnAttente = data,
      error: (err) => console.error('Erreur:', err)
    });
    
    // Charger les avocats
    this.adminService.getAvocats().subscribe({
      next: (data) => this.avocats = data,
      error: (err) => console.error('Erreur:', err)
    });
  }
trackByUserId(index: number, user: any): number {
  return user.id;
}

  calculerStats(): void {
    this.stats.total = this.tousLesUtilisateurs.length;
    this.stats.avocats = this.tousLesUtilisateurs.filter(u => u.role === 'AVOCAT').length;
    this.stats.clients = this.tousLesUtilisateurs.filter(u => u.role === 'CLIENT').length;
    this.stats.actifs = this.tousLesUtilisateurs.filter(u => u.statut === 'ACTIF').length;
 this.stats.bloques = this.tousLesUtilisateurs.filter(u => u.statut === 'BLOQUE').length;
  this.stats.enAttente = this.tousLesUtilisateurs.filter(u => u.statut === 'EN_ATTENTE').length;
  }

  filtrerUtilisateurs(): void {
    let resultats = [...this.tousLesUtilisateurs];
    
    if (this.filtreActif !== 'tous') {
      switch(this.filtreActif) {
        case 'ACTIF': resultats = resultats.filter(u => u.statut === 'ACTIF'); break;
        case 'BLOQUE': resultats = resultats.filter(u => u.statut === 'BLOQUE'); break;
      case 'AVOCAT': resultats = resultats.filter(u => u.role === 'AVOCAT'); break;
      case 'CLIENT': resultats = resultats.filter(u => u.role === 'CLIENT'); break;
      }
    }
    
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      const term = this.searchTerm.toLowerCase().trim();
      resultats = resultats.filter(u =>
        (u.nom && u.nom.toLowerCase().includes(term)) ||
        (u.prenom && u.prenom.toLowerCase().includes(term)) ||
        (u.email && u.email.toLowerCase().includes(term)) ||
        (u.telephone && u.telephone.includes(term))
      );
    }
    
    this.utilisateursFiltres = resultats;
  }

  changerFiltre(filtre: string): void {
    this.filtreActif = filtre;
    this.filtrerUtilisateurs();
  }

  onSearchChange(): void {
    this.filtrerUtilisateurs();
  }

  getIconForRole(role: string): string {
    switch(role) {
      case 'AVOCAT': return 'gavel';
      case 'CLIENT': return 'person';
      default: return 'admin_panel_settings';
    }
  }

  voirDetails(user: any): void {
    this.selectedUser = user;
  }

  accepterUtilisateur(id: number): void {
    this.adminService.accepter(id).subscribe({
      next: () => {
        this.snackBar.open('Utilisateur activé !', 'OK', { duration: 3000 });
        this.chargerDonnees();
      },
      error: () => this.snackBar.open('Erreur', 'OK', { duration: 3000 })
    });
  }

  refuserUtilisateur(id: number): void {
    const motif = prompt("Motif du refus :");
    if (motif) {
      this.adminService.refuser(id, motif).subscribe({
        next: () => {
          this.snackBar.open('Utilisateur refusé', 'OK', { duration: 3000 });
          this.chargerDonnees();
        },
        error: () => this.snackBar.open('Erreur', 'OK', { duration: 3000 })
      });
    }
  }

// 2. Modifie la fonction bloquerUtilisateur pour une mise à jour visuelle instantanée
bloquerUtilisateur(user: any): void {
  const ancienStatut = user.statut;
  const nouveauStatut = ancienStatut === 'ACTIF' ? 'BLOQUE' : 'ACTIF';
  const action = nouveauStatut === 'ACTIF' ? 'débloqué' : 'bloqué';

  // Mise à jour locale immédiate (Optimistic UI)
  user.statut = nouveauStatut;

  this.adminService.changerStatut(user.id, nouveauStatut).subscribe({
    next: () => {
      this.snackBar.open(`Utilisateur ${action} avec succès`, 'Fermer', { duration: 3000 });
      // On recalcule les stats sans tout recharger
      this.calculerStats();
    },
    error: (err) => {
      // En cas d'erreur, on remet l'ancien statut
      user.statut = ancienStatut;
      console.error('Erreur API:', err);
      this.snackBar.open('Erreur lors du changement de statut', 'Fermer', { duration: 5000 });
    }
  });
}


}