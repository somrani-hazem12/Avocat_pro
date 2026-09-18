import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LegalDocumentService {
  private apiUrl = 'http://localhost:8080/api/legal-documents';

  constructor(private http: HttpClient) {}

  // Générer un document PDF
  generer(request: any): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/generate`, request, {
      responseType: 'blob'
    });
  }

}