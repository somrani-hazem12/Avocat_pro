import { Component, OnInit, OnDestroy, HostListener } from '@angular/core'; // Import de HostListener
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { NotificationService } from '../../services/notification.service';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth'; 

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule, RouterModule, MatListModule,
    MatIconModule, MatDividerModule, MatBadgeModule,
    MatButtonModule
  ],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class SidebarComponent implements OnInit, OnDestroy {
  nom = localStorage.getItem('nom') || 'Utilisateur';
  role = localStorage.getItem('role') || '';
  notifCount = 0;
  private notifSub!: Subscription;

  // --- VARIABLES SÉCURITÉ INACTIVITÉ ---
  timeoutId: any;
  private INACTIVITY_TIME = 0.5 * 60 * 1000; // 15 minutes

  allMenuItems = [
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/dashboard',
      roles: ['ADMINISTRATEUR', 'AVOCAT', 'ASSISTANT', 'CLIENT']
    },
    {
      label: 'Mes Clients',
      icon: 'people',
      route: '/clients',
      roles: ['AVOCAT', 'ASSISTANT']
    },
    {
      label: 'Dossiers',
      icon: 'folder',
      route: '/dossiers',
      roles: ['AVOCAT', 'ASSISTANT', 'CLIENT']
    },
    {
      label: 'Factures',
      icon: 'receipt',
      route: '/factures',
      roles: ['AVOCAT', 'ASSISTANT', 'CLIENT']
    },
    {
      label: 'Rendez-vous',
      icon: 'event',
      route: '/rendez-vous',
      roles: ['AVOCAT', 'ASSISTANT', 'CLIENT']
    },
    {
      label: 'Détection Risques',
      icon: 'security',
      route: '/risk',
      roles: ['AVOCAT', 'ASSISTANT', 'ADMINISTRATEUR']
    },
    {
      label: 'Notifications',
      icon: 'notifications',
      route: '/notifications',
      roles: ['AVOCAT'],
      badge: true 
    },
    {
      label: 'Utilisateurs',
      icon: 'manage_accounts',
      route: '/utilisateurs',
      roles: ['ADMINISTRATEUR']
    },
    {
      label: 'Logs',
      icon: 'history',
      route: '/logs',
      roles: ['ADMINISTRATEUR']
    },
  ];

  menuItems: any[] = [];

  constructor(
    private router: Router,
    private notifService: NotificationService,
    private authService: AuthService
  ) { }

  // --- LOGIQUE DE DÉTECTION D'ACTIVITÉ ---

  // Écoute les mouvements de souris, clics et touches clavier partout dans la fenêtre
  @HostListener('window:mousemove')
  @HostListener('window:click')
  @HostListener('window:keydown')
  @HostListener('window:scroll')
  onUserActivity() {
    this.resetTimer();
  }

  resetTimer() {
    // Si un chrono est déjà lancé, on l'annule pour le recommencer à zéro
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
    
    // On lance le compte à rebours de 15 minutes
    this.timeoutId = setTimeout(() => {
      this.handleAutoLogout();
    }, this.INACTIVITY_TIME);
  }

  handleAutoLogout() {
    // On ne déconnecte que si un token existe (utilisateur logué)
    if (localStorage.getItem('token')) {
      alert("⚠️ Votre session a expiré pour cause d'inactivité (Sécurité AVOCAT-PRO).");
      this.logout();
    }
  }

  // --- MÉTHODES INITIALES ---

  ngOnInit() {
    this.menuItems = this.allMenuItems.filter(item =>
      item.roles.includes(this.role)
    );

    // S'abonner au compteur de notifications
    this.notifSub = this.notifService.count$.subscribe(count => {
      this.notifCount = count;
    });

    // Démarrer le chrono d'inactivité dès le chargement de la sidebar
    this.resetTimer(); 
  }

  ngOnDestroy() {
    // Nettoyage des abonnements et du chrono pour éviter les fuites de mémoire
    if (this.notifSub) this.notifSub.unsubscribe();
    if (this.timeoutId) clearTimeout(this.timeoutId);
  }

  logout() {
    // Nettoyer les données et rediriger
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  isActive(route: string): boolean {
    return this.router.url === route;
  }
}