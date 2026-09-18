import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router'; 
import { DossierService } from '../../services/dossier';
import { DocumentService } from '../../services/document';
import { UtilisateurService } from '../../services/utilisateur';
import { SidebarComponent } from '../../components/sidebar/sidebar';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-dossiers',
  standalone: true,
  imports: [
    CommonModule, FormsModule, SidebarComponent, MatTableModule, 
    MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, 
    MatInputModule, MatSelectModule, MatSnackBarModule, MatListModule, 
    MatTooltipModule, MatDividerModule
  ],
  templateUrl: './dossiers.html',
  styleUrls: ['./dossiers.css']
})
export class DossiersComponent implements OnInit {
  dossiers: any[] = [];
  mesClients: any[] = [];
  role = localStorage.getItem('role');
  
  // FIX : AJOUT DE LA COLONNE 'client' DANS LE TABLEAU
  displayedColumns: string[] = ['reference', 'client', 'type', 'statut', 'date', 'actions'];

  // État du filtrage
  isFiltered = false;
  clientIdFiltre: number | null = null;
 selectedDocSummary: string | null = null; // Stocke le résumé actuel
  isSummarizingId: number | null = null;    // Pour le chargement par document

  selectedDossier: any = null;
  documents: any[] = [];
  loadingDocs = false;

  showForm = false;
  newDossier = { reference: '', type: '', statut: 'OUVERT', clientId: null as number | null };

  constructor(
    private dossierService: DossierService,
    private userService: UtilisateurService, 
    private documentService: DocumentService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

    ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const id = params['clientId'];
      if (id) {
        console.log("Filtrage activé pour le client ID :", id);
        this.isFiltered = true;
        this.clientIdFiltre = +id;
        this.chargerDossiers(this.clientIdFiltre); // Envoi de l'ID au service
      } else {
        this.isFiltered = false;
        this.clientIdFiltre = null;
        this.chargerDossiers();
      }
    });

    if (this.role === 'AVOCAT') {
      this.chargerMesClients();
    }
  }

  chargerDossiers(clientId?: number) {
    // On appelle le service en passant l'ID (le service ajoutera ?clientId=... à l'URL)
    this.dossierService.getDossiers(clientId).subscribe({
      next: (data) => {
        this.dossiers = data;
      },
      error: (err) => console.error("Erreur chargement dossiers", err)
    });
  }

 reinitialiserFiltre() {
  this.isFiltered = false;
  this.clientIdFiltre = null;
  // On navigue vers la page sans paramètres, ce qui va forcer le ngOnInit à charger TOUS les dossiers
  this.router.navigate(['/dossiers'], { queryParams: {} });
}

  chargerMesClients() {
    this.userService.getMesClients().subscribe(data => this.mesClients = data);
  }

  // --- DOCUMENTS ---

  ouvrirDossier(dossier: any) {
    this.selectedDossier = dossier;
    this.chargerDocuments(dossier.id);
  }

  chargerDocuments(dossierId: number) {
    this.loadingDocs = true;
    this.documentService.getDocuments(dossierId).subscribe({
      next: (docs) => { this.documents = docs; this.loadingDocs = false; },
      error: () => this.loadingDocs = false
    });
  }

  consulterDocument(doc: any) {
    this.documentService.downloadFile(doc.id).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
      },
      error: () => this.snackBar.open("Erreur d'accès au document", "Fermer")
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file && this.selectedDossier) {
      const uploader = localStorage.getItem('nom') || 'Utilisateur';
      this.documentService.upload(this.selectedDossier.id, file, uploader).subscribe(() => {
        this.snackBar.open('Fichier ajouté', 'OK', { duration: 2000 });
        this.chargerDocuments(this.selectedDossier.id);
      });
    }
  }

  // --- ACTIONS ---

  ajouterDossier() {
    if (!this.newDossier.clientId || !this.newDossier.reference) return;

    const payload = {
      reference: this.newDossier.reference,
      type: this.newDossier.type,
      statut: this.newDossier.statut,
      client: { id: this.newDossier.clientId }
    };

    this.dossierService.creerDossier(payload).subscribe(() => {
      this.snackBar.open('Dossier créé !', 'OK', { duration: 2000 });
      this.showForm = false;
      this.newDossier = { reference: '', type: '', statut: 'OUVERT', clientId: null };
      this.chargerDossiers();
    });
  }


  resumerAI(doc: any) {
    this.selectedDocSummary = null;
    this.isSummarizingId = doc.id;
    
    this.documentService.summarize(doc.id).subscribe({
      next: (res: string) => { // Déjà typé
        this.selectedDocSummary = res;
        this.isSummarizingId = null;
        this.snackBar.open("Résumé généré avec succès", "OK", { duration: 3000 });
      },
      error: (err: any) => { // Déjà typé
        console.error(err);
        this.snackBar.open("Erreur lors de la génération du résumé", "Fermer");
        this.isSummarizingId = null;
      }
    });
  }

}