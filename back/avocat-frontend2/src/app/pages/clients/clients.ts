import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router'; 
import { UtilisateurService } from '../../services/utilisateur';
import { SidebarComponent } from '../../components/sidebar/sidebar';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [
    CommonModule, FormsModule, SidebarComponent, MatTableModule, 
    MatButtonModule, MatIconModule, MatCardModule, MatFormFieldModule, 
    MatInputModule, MatSnackBarModule, MatTooltipModule
  ],
  templateUrl: './clients.html',
  styleUrls: ['./clients.css']
})
export class ClientsComponent implements OnInit {
  clients: any[] = [];
  displayedColumns: string[] = ['nom', 'email', 'telephone', 'adresse', 'actions'];
  showForm = false;
  editMode = false;
  selectedId: number | null = null;
  form = { nom: '', email: '', telephone: '', adresse: '' };

  constructor(private userService: UtilisateurService, private router: Router, private snackBar: MatSnackBar) {}

  ngOnInit(): void { this.chargerClients(); }

  chargerClients() {
    this.userService.getMesClients().subscribe(data => this.clients = data);
  }

  ouvrirDossier(clientId: number) {
    this.router.navigate(['/dossiers'], { queryParams: { clientId: clientId } });
  }

  openForm() {
    this.showForm = true;
    this.editMode = false;
    this.form = { nom: '', email: '', telephone: '', adresse: '' };
  }

  edit(client: any) {
    this.showForm = true;
    this.editMode = true;
    this.selectedId = client.id;
    this.form = { ...client }; // FIX : Copie pour éviter de modifier la ligne visuelle avant sauvegarde
  }

  save() {
    if (this.editMode && this.selectedId !== null) {
      // FIX : On force le type 'number' pour selectedId
      this.userService.updateClient(this.selectedId as number, this.form).subscribe({
        next: () => {
          this.snackBar.open("Fiche client mise à jour", "OK", { duration: 2000 });
          this.showForm = false;
          this.chargerClients();
        }
      });
    } else {
      this.userService.ajouterClientManual(this.form).subscribe({
        next: () => {
          this.snackBar.open("Nouveau client ajouté", "OK", { duration: 2000 });
          this.showForm = false;
          this.chargerClients();
        }
      });
    }
  }

  delete(id: number) {
    if (confirm('Voulez-vous retirer ce client ?')) {
      this.userService.retirerAvocat(id).subscribe(() => {
        this.snackBar.open("Client retiré", "OK", { duration: 2000 });
        this.chargerClients(); // FIX : Fait disparaître la ligne
      });
    }
  }

  cancel() { this.showForm = false; }
}