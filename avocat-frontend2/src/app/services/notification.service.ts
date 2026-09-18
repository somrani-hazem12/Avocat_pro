import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = 'http://localhost:8080/api/notifications';
  private countSubject = new BehaviorSubject<number>(0);
  count$ = this.countSubject.asObservable();

  constructor(private http: HttpClient) {
    this.startPolling();
  }

  // Polling toutes les 30 secondes
  private startPolling() {
    interval(30000).pipe(
      switchMap(() => this.getCount())
    ).subscribe(data => {
      this.countSubject.next(data.count);
    });
    // Premier appel immédiat
    this.getCount().subscribe(data => {
      this.countSubject.next(data.count);
    });
  }

  getAll(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  getNonLues(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/non-lues`);
  }

  getCount(): Observable<any> {
    return this.http.get(`${this.apiUrl}/count`);
  }

  marquerLue(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/lire`, {});
  }

  marquerToutLu(): Observable<any> {
    return this.http.put(`${this.apiUrl}/lire-tout`, {});
  }

  supprimer(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  getSeuils(): Observable<any> {
    return this.http.get(`${this.apiUrl}/seuils`);
  }

  saveSeuils(seuils: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/seuils`, seuils);
  }

  refreshCount() {
    this.getCount().subscribe(data => {
      this.countSubject.next(data.count);
    });
  }
}