import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class FactureService {
  private apiUrl = 'http://localhost:8080/api/factures';

  constructor(private http: HttpClient) {}

  getAll(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  create(facture: any): Observable<any> {
    return this.http.post(this.apiUrl, facture);
  }

  // CETTE MÉTHODE MANQUAIT :
  marquerCommePayee(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/payer`, {});
  }
  downloadPdf(id: number): Observable<Blob> {
  return this.http.get(`http://localhost:8080/api/factures/${id}/pdf`, {
    responseType: 'blob' // TRÈS IMPORTANT pour les fichiers binaires
  });
}
}