import { Injectable } from '@angular/core';
import { HttpClient,HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DossierService {
  private apiUrl = 'http://localhost:8080/api/dossiers';

  constructor(private http: HttpClient) {}


  // Supprimer un dossier
  supprimer(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  getDossiers(clientId?: number): Observable<any[]> {
    let params = new HttpParams();
    if (clientId) {
      params = params.set('clientId', clientId.toString());
    }
    return this.http.get<any[]>(this.apiUrl, { params });
  }

  creerDossier(dossier: any): Observable<any> {
    return this.http.post(this.apiUrl, dossier);
  }












}