// ... imports identiques ...

import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { Observable, tap } from "rxjs";

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, motDePasse: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, { email, motDePasse }).pipe(
      tap((response: any) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('nom', response.nom);
        localStorage.setItem('id', response.id.toString()); // Stockage de l'ID crucial
      })
    );
  }

  register(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, data, { responseType: 'text' });
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  // Getters
  getToken() { return localStorage.getItem('token'); }
  getRole() { return localStorage.getItem('role'); }
  getUserId() { return localStorage.getItem('id'); }
  isLoggedIn() { return !!this.getToken(); }
}