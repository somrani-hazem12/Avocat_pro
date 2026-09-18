import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../services/admin';
import { SidebarComponent } from '../../components/sidebar/sidebar';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-utilisateurs',
  standalone: true,
  imports: [
    CommonModule, 
    SidebarComponent, 
    MatTableModule, 
    MatButtonModule, 
    MatIconModule, 
    MatCardModule, 
    MatSnackBarModule
  ],
  templateUrl: './utilisateurs.html',
  styleUrls: ['./utilisateurs.css']
})
export class UtilisateursComponent implements OnInit {
  utilisateursEnAttente: any[] = [];
  // MISE À JOUR : On ajoute avocats et clients
  stats: any = { total: 0, enAttente: 0, actifs: 0, avocats: 0, clients: 0 };
  colonnesAffichees: string[] = ['nom', 'email', 'role', 'date', 'actions'];

  constructor(private adminService: AdminService, private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.chargerDonnees();
  }

  chargerDonnees(): void {
    this.adminService.getEnAttente().subscribe((data: any) => {
      this.utilisateursEnAttente = data;
    });
    this.adminService.getStats().subscribe((data: any) => {
      this.stats = data; // Le backend envoie maintenant tout l'objet
    });
  }

  accepterUtilisateur(id: number): void {
    this.adminService.accepter(id).subscribe(() => {
      this.snackBar.open('Utilisateur activé !', 'OK', { duration: 3000 });
      this.chargerDonnees();
    });
  }

  refuserUtilisateur(id: number): void {
    const motif = prompt("Motif du refus :");
    if (motif) {
      this.adminService.refuser(id, motif).subscribe(() => {
        this.snackBar.open('Utilisateur refusé', 'OK', { duration: 3000 });
        this.chargerDonnees();
      });
    }
  }
}