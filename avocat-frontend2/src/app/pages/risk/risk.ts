import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../../components/sidebar/sidebar';
import { RiskService } from '../../services/risk';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-risk',
  standalone: true,
  imports: [
    CommonModule, SidebarComponent,
    MatCardModule, MatIconModule,
    MatBadgeModule, MatChipsModule
  ],
  templateUrl: './risk.html',
  styleUrl: './risk.css'
})
export class RiskComponent implements OnInit {
  dashboard: any = null;
  loading = true;

  constructor(private riskService: RiskService) {}

  ngOnInit() {
    this.riskService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  getCouleur(niveau: string): string {
    if (niveau === 'Faible') return 'success';
    if (niveau === 'Moyen') return 'warn';
    return 'danger';
  }

  getIcon(niveau: string): string {
    if (niveau === 'Faible') return 'check_circle';
    if (niveau === 'Moyen') return 'warning';
    return 'error';
  }
  getNiveauClass(niveau: string): string {
  if (niveau === 'Faible') return 'faible';
  if (niveau === 'Moyen') return 'moyen';
  return 'eleve';
}
}