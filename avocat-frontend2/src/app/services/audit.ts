import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuditService {
  // CORRECTION : L'URL doit correspondre au @RequestMapping du LogController
  private apiUrl = 'http://localhost:8080/api/logs'; 

  constructor(private http: HttpClient) {}

  getLogs(): Observable<any[]> {
    // On appelle directement la racine de l'API logs
    return this.http.get<any[]>(this.apiUrl); 
  }
}