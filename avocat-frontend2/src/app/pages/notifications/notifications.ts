import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SidebarComponent } from '../../components/sidebar/sidebar';
import { NotificationService } from '../../services/notification.service';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSliderModule } from '@angular/material/slider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [
    CommonModule, FormsModule, SidebarComponent,
    MatCardModule, MatIconModule, MatButtonModule,
    MatTabsModule, MatSliderModule, MatSnackBarModule
  ],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css'
})
export class NotificationsComponent implements OnInit {
  notifications: any[] = [];
  seuils: any = {
    seuilInactivite: 15,
    seuilDeadline: 5,
    seuilDocuments: 2,
    seuilAnciennete: 90
  };
  loading = true;

  constructor(
    private notifService: NotificationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadNotifications();
    this.loadSeuils();
  }

  loadNotifications() {
    this.notifService.getAll().subscribe({
      next: (data) => { this.notifications = data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  loadSeuils() {
    this.notifService.getSeuils().subscribe({
      next: (data) => this.seuils = data
    });
  }

  marquerLue(id: number) {
    this.notifService.marquerLue(id).subscribe(() => {
      this.notifications = this.notifications.map(n =>
        n.id === id ? { ...n, lue: true } : n
      );
      this.notifService.refreshCount();
    });
  }

  marquerToutLu() {
    this.notifService.marquerToutLu().subscribe(() => {
      this.notifications = this.notifications.map(n => ({ ...n, lue: true }));
      this.notifService.refreshCount();
      this.snackBar.open('Toutes les notifications marquées comme lues', 'OK', { duration: 3000 });
    });
  }

  supprimer(id: number) {
    this.notifService.supprimer(id).subscribe(() => {
      this.notifications = this.notifications.filter(n => n.id !== id);
      this.notifService.refreshCount();
    });
  }

  saveSeuils() {
    this.notifService.saveSeuils(this.seuils).subscribe(() => {
      this.snackBar.open('Seuils sauvegardés avec succès !', 'OK', { duration: 3000 });
    });
  }

  getIcon(type: string): string {
    const icons: any = {
      'DEADLINE': 'schedule',
      'INACTIVITE': 'hourglass_empty',
      'DOCUMENT': 'description',
      'RISQUE': 'security'
    };
    return icons[type] || 'notifications';
  }

  getNiveauClass(niveau: string): string {
    if (niveau === 'FAIBLE') return 'faible';
    if (niveau === 'MOYEN') return 'moyen';
    return 'eleve';
  }

  getNonLues(): number {
    return this.notifications.filter(n => !n.lue).length;
  }
}