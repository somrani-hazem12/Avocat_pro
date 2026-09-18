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
import { LegalDocumentService } from '../../services/LegalDocument';
import * as L from 'leaflet';
import { HttpClient } from '@angular/common/http'; // Pour appeler l'API de géocodage
// Fix pour les icônes Leaflet cassées dans Angular
const iconDefault = L.icon({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41]
});

L.Marker.prototype.options.icon = iconDefault;
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
  selectedTemplate = '';
  docFormValues: any = {};
    listeClients: any[] = []; 
      configChamps: any[] = [];
        private map!: L.Map;
  private markers: L.Marker[] = [];
  templatesConfig = [
    { id: 'mise-en-demeure', label: 'Mise en demeure', fields: [
        { id: 'nomClient', label: 'Choisir le Client', type: 'select_client' },
        { id: 'numeroDossier', label: 'Choisir le Dossier', type: 'text' },
        { id: 'objet', label: 'Objet du litige', type: 'text' },
        { id: 'montant', label: 'Montant dû', type: 'text' },
        { id: 'nomAvocat', label: 'Avocat (Auto)', type: 'readonly' }
    ]},
    { id: 'contrat-travail', label: 'Contrat de travail', fields: [
        { id: 'nomClient', label: 'Nom du Salarié', type: 'select_client' },
        { id: 'poste', label: 'Poste proposé', type: 'text' },
        { id: 'salaire', label: 'Salaire', type: 'text' },
        { id: 'nomAvocat', label: 'Employeur (Auto)', type: 'readonly' }
    ]},
    { id: 'procuration', label: 'Procuration', fields: [
        { id: 'nomClient', label: 'Mandant', type: 'select_client' },
        { id: 'numeroDossier', label: 'N° Dossier', type: 'text' },
        { id: 'nomAvocat', label: 'Mandataire (Auto)', type: 'readonly' }
    ]}
  ];
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

  constructor(private userService: UtilisateurService, private legalDocService: LegalDocumentService,private snackBar: MatSnackBar,  private http: HttpClient) {}

  ngOnInit() {
    this.chargerDonnees();
         this.chargerClients(); 
  }
  chargerClients() {
    this.userService.getMesClients().subscribe(data => {
      this.listeClients = data;
      
      // On attend que le rôle soit vérifié et le DOM chargé
      if (this.role === 'AVOCAT') {
        setTimeout(() => {
          this.initMap();
          this.markers.forEach(m => this.map.removeLayer(m)); // Nettoyer anciens marqueurs
          this.markers = [];
          
          this.listeClients.forEach(client => {
            this.ajouterClientSurCarte(client);
          });
        }, 500);
      }
    });
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

  onTemplateChange() {
    const config = this.templatesConfig.find(t => t.id === this.selectedTemplate);
    this.configChamps = config ? config.fields : [];
    
    this.docFormValues = {};
    this.configChamps.forEach(field => {
      if (field.id === 'nomAvocat') {
        this.docFormValues[field.id] = "Maître " + this.nom; // AUTO-REMPLISSAGE
      } else {
        this.docFormValues[field.id] = '';
      }
    });
    this.docFormValues['date'] = new Date().toLocaleDateString('fr-FR');
  }

  genererDocument() {
    this.legalDocService.generer({ templateId: this.selectedTemplate, data: this.docFormValues }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${this.selectedTemplate}.pdf`;
        a.click();
      },
      error: () => this.snackBar.open("Erreur de génération PDF", "OK")
    });
  }



  private initMap(): void {
    if (this.map) { this.map.remove(); } // Reset si elle existe déjà

    this.map = L.map('map').setView([33.8869, 9.5375], 6); // Centré sur la Tunisie par défaut

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
  }

  // Cette fonction transforme l'adresse en coordonnées et place un marqueur
  private ajouterClientSurCarte(client: any) {
    if (!client.adresse) return;

    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(client.adresse)}`;
    
    this.http.get<any[]>(url).subscribe(results => {
      if (results && results.length > 0) {
        const lat = results[0].lat;
        const lon = results[0].lon;

        const marker = L.marker([lat, lon])
          .addTo(this.map)
          .bindPopup(`
            <b>Client : ${client.nom}</b><br>
            ${client.adresse}<br>
            <small>Tél: ${client.telephone || 'N/A'}</small>
          `);
        
        this.markers.push(marker);
      }
    });
  }




}