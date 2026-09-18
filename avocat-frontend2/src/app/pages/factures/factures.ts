import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { FormsModule } from '@angular/forms';
import { FactureService } from '../../services/facture';
import { UtilisateurService } from '../../services/utilisateur'; // FIX : Utiliser UtilisateurService
import { SidebarComponent } from '../../components/sidebar/sidebar';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-factures',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    SidebarComponent, 
    MatTableModule, 
    MatCardModule, 
    MatButtonModule, 
    MatIconModule, 
    MatFormFieldModule, 
    MatInputModule, 
    MatSelectModule, 
    MatSnackBarModule,
    MatTooltipModule
  ],
  templateUrl: './factures.html',
  styleUrls: ['./factures.css']
})
export class FacturesComponent implements OnInit {
  factures: any[] = [];
  mesClients: any[] = []; // Cette liste contiendra vos clients rattachés
  role = localStorage.getItem('role');
  displayedColumns: string[] = ['date', 'client', 'montant', 'statut', 'actions'];

  showForm = false;
  // Modèle pour une nouvelle facture
  newFacture = { montantHT: 0, tva: 19, clientId: null as number | null };

  constructor(
    private factureService: FactureService,
    private userService: UtilisateurService, // Injecté
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.chargerFactures();
    if (this.role === 'AVOCAT') {
      this.chargerMesClients();
    }
  }

  chargerFactures() {
    this.factureService.getAll().subscribe({
      next: (data) => this.factures = data,
      error: (err) => console.error("Erreur chargement factures", err)
    });
  }

  /**
   * FIX : Charger uniquement les clients de cet avocat pour le menu déroulant
   */
  chargerMesClients() {
    this.userService.getMesClients().subscribe({
      next: (data) => {
        this.mesClients = data;
        console.log("Clients chargés pour la facturation :", this.mesClients.length);
      },
      error: (err) => console.error("Erreur dropdown factures", err)
    });
  }

  creerFacture() {
    if (!this.newFacture.clientId || this.newFacture.montantHT <= 0) {
      this.snackBar.open("Veuillez remplir tous les champs", "OK");
      return;
    }

    const payload = {
      montantHT: this.newFacture.montantHT,
      tva: this.newFacture.tva,
      client: { id: this.newFacture.clientId }
    };

    this.factureService.create(payload).subscribe({
      next: () => {
        this.snackBar.open('Facture générée avec succès', 'OK', { duration: 3000 });
        this.showForm = false;
        this.newFacture = { montantHT: 0, tva: 19, clientId: null };
        this.chargerFactures();
      },
      error: (err) => this.snackBar.open("Erreur de création", "Fermer")
    });
  }

  payer(id: number) {
    this.factureService.marquerCommePayee(id).subscribe(() => {
      this.snackBar.open('Facture marquée comme PAYÉE', 'OK', { duration: 2000 });
      this.chargerFactures();
    });
  }

  telechargerFacture(id: number) {
    this.factureService.downloadPdf(id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `Facture_${id}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => this.snackBar.open("Erreur de téléchargement", "Fermer")
    });
  }
}