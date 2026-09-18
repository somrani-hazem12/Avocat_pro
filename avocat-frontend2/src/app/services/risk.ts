import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RiskService {
  private apiUrl = 'http://localhost:8080/api/risk';

  constructor(private http: HttpClient) {}

  analyserDossier(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/dossier/${id}`);
  }

  analyserMesDossiers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/mes-dossiers`);
  }

  getDashboard(): Observable<any> {
    return this.http.get(`${this.apiUrl}/dashboard`);
  }
}