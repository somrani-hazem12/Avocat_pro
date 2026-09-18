import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  getEnAttente(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/en-attente`);
  }

  getStats(): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats`);
  }

  accepter(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/accepter/${id}`, {}, { responseType: 'text' });
  }

  refuser(id: number, motif: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/refuser/${id}`, { motif }, { responseType: 'text' });
  }
}