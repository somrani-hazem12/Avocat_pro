import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatListModule, MatIconModule, MatDividerModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class SidebarComponent implements OnInit {
  nom = localStorage.getItem('nom') || 'Utilisateur';
  role = localStorage.getItem('role') || '';

  // Liste complète des menus avec les rôles autorisés pour chaque lien
  allMenuItems = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard', roles: ['ADMINISTRATEUR', 'AVOCAT', 'ASSISTANT', 'CLIENT'] },
    { label: 'Mes Clients', icon: 'people', route: '/clients', roles: ['AVOCAT', 'ASSISTANT'] },
    { label: 'Dossiers', icon: 'folder', route: '/dossiers', roles: ['AVOCAT', 'ASSISTANT', 'CLIENT'] },
    { label: 'Factures', icon: 'receipt', route: '/factures', roles: ['AVOCAT', 'ASSISTANT', 'CLIENT'] },
    { label: 'Rendez-vous', icon: 'event', route: '/rendez-vous', roles: ['AVOCAT', 'ASSISTANT', 'CLIENT'] },
    { label: 'Utilisateurs', icon: 'manage_accounts', route: '/utilisateurs', roles: ['ADMINISTRATEUR'] },
    { label: 'Logs', icon: 'history', route: '/logs', roles: ['ADMINISTRATEUR'] },
  ];

  // Cette liste contiendra uniquement les menus autorisés
  menuItems: any[] = [];

  constructor(private router: Router) {}

  ngOnInit() {
    // FILTRAGE : On ne garde que les menus où le rôle de l'utilisateur est présent dans 'roles'
    this.menuItems = this.allMenuItems.filter(item => item.roles.includes(this.role));
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  isActive(route: string): boolean {
    return this.router.url === route;
  }
}