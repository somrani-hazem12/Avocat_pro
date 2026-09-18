import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UtilisateurService {
  private apiUrl = 'http://localhost:8080/api/utilisateur';
  private adminUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  // --- MÉTHODES COMMUNES ---
  getProfile(): Observable<any> { 
    return this.http.get(`${this.apiUrl}/profile`); 
  }

  // --- MÉTHODES CLIENT ---
  getAvocatsDisponibles(): Observable<any[]> { 
    return this.http.get<any[]>(`${this.apiUrl}/avocats-disponibles`); 
  }
  choisirAvocat(id: number): Observable<any> { 
    return this.http.post(`${this.apiUrl}/choisir-avocat/${id}`, {}); 
  }
  retirerAvocat(id: number): Observable<any> { 
    return this.http.delete(`${this.apiUrl}/retirer-avocat/${id}`); 
  }

  // --- MÉTHODES AVOCAT ---
  getStatsCabinet(): Observable<any> { 
    return this.http.get(`${this.apiUrl}/stats-cabinet`); 
  }
  getMesClients(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/mes-clients`);
  }
  ajouterClientManual(client: any): Observable<any> { // FIX NOM
    return this.http.post(`${this.apiUrl}/ajouter-client-manuel`, client);
  }
  updateClient(id: number, client: any): Observable<any> { // FIX MANQUANT
    return this.http.put(`${this.apiUrl}/modifier-client/${id}`, client);
  }

  // --- MÉTHODES ADMINISTRATEUR ---
  getStatsGlobales(): Observable<any> {
    return this.http.get(`${this.adminUrl}/stats`);
  }
  getAllUsers(): Observable<any[]> { 
    return this.http.get<any[]>(`${this.adminUrl}/utilisateurs`); 
  }
  getUsersEnAttente(): Observable<any[]> {
    return this.http.get<any[]>(`${this.adminUrl}/en-attente`);
  }
  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${this.adminUrl}/supprimer/${id}`);
  }
  updateUser(id: number, data: any): Observable<any> {
    return this.http.put(`${this.adminUrl}/modifier/${id}`, data);
  }
  createUser(data: any): Observable<any> {
    return this.http.post(`${this.adminUrl}/creer`, data);
  }
  validerUser(id: number): Observable<any> {
    return this.http.put(`${this.adminUrl}/accepter/${id}`, {});
  }
}