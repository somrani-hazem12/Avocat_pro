import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuditService } from '../../services/audit';
import { SidebarComponent } from '../../components/sidebar/sidebar';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [
    CommonModule, SidebarComponent, MatCardModule, 
    MatIconModule, MatTableModule, MatTooltipModule
  ],
  templateUrl: './logs.html',
  styleUrl: './logs.css',
})
export class Logs implements OnInit {
  auditLogs: any[] = [];
  // Toutes les colonnes de ta table SQL
  displayedColumns: string[] = ['id', 'dateAction', 'utilisateur', 'action', 'cible', 'adresseIp', 'details'];
  loading = true;

  constructor(private auditService: AuditService) {}

  ngOnInit(): void {
    this.chargerLogs();
  }

  chargerLogs() {
    this.loading = true;
    this.auditService.getLogs().subscribe({
      next: (data) => {
        this.auditLogs = data;
        this.loading = false;
      },
      error: (err) => {
        console.error("Erreur logs", err);
        this.loading = false;
      }
    });
  }
}