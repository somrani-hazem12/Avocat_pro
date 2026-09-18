import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RendezVousService {
  private apiUrl = 'http://localhost:8080/api/rendez-vous';

  constructor(private http: HttpClient) {}

  getAll(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // MÉTHODE POUR CRÉER (Nom : creer)
  creer(rdv: any): Observable<any> {
    return this.http.post(this.apiUrl, rdv);
  }

  // MÉTHODE POUR CONFIRMER (Nom : confirmer)
  confirmer(id: number, date: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/confirmer`, { dateHeure: date });
  }
}