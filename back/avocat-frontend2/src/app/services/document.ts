import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private apiUrl = 'http://localhost:8080/api/documents';

  constructor(private http: HttpClient) {}

  upload(dossierId: number, file: File, uploader: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('uploader', uploader);
    return this.http.post(`${this.apiUrl}/upload/${dossierId}`, formData);
  }

  getDocuments(dossierId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/dossier/${dossierId}`);
  }

  // MÉTHODE SÉCURISÉE : Télécharge le fichier en tant que BLOB (données binaires)
  downloadFile(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download/${id}`, {
      responseType: 'blob' 
    });
  }

  getDownloadUrl(id: number): string {
    return `${this.apiUrl}/download/${id}`;
  }
}