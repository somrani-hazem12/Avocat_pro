import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getEnAttente(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/en-attente`, { headers: this.getHeaders() });
  }

  getStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats`, { headers: this.getHeaders() });
  }

  getAllUtilisateurs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/utilisateurs`, { headers: this.getHeaders() });
  }

  getAvocats(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/clients/avocats-disponibles', { 
      headers: this.getHeaders() 
    });
  }

  accepter(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/accepter/${id}`, {}, { headers: this.getHeaders() });
  }

  refuser(id: number, motif: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/refuser/${id}`, { motif }, { headers: this.getHeaders() });
  }

  changerStatut(id: number, statut: string): Observable<any> {
    console.log('Appel API changement statut:', `${this.apiUrl}/utilisateurs/${id}/statut`, statut);
    return this.http.put(`${this.apiUrl}/utilisateurs/${id}/statut`, { statut }, { headers: this.getHeaders() });
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/utilisateurs/${id}`, { headers: this.getHeaders() });
  }
}