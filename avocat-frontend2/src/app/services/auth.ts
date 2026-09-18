import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { Observable, tap } from "rxjs";

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, motDePasse: string): Observable<any> {
    console.log('=== LOGIN ===');
    return this.http.post(`${this.apiUrl}/login`, { email, motDePasse }).pipe(
      tap((response: any) => {
        if (response && response.token) {
          localStorage.setItem('token', response.token);
          localStorage.setItem('role', response.role);
          localStorage.setItem('email', response.email);
          console.log('Connexion réussie, token stocké');
        }
      })
    );
  }

  register(userData: any): Observable<any> {
    console.log('=== REGISTER ===', userData);
    return this.http.post(`${this.apiUrl}/register`, userData, { responseType: 'text' });
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }

  getToken(): string | null { return localStorage.getItem('token'); }
  getRole(): string | null { return localStorage.getItem('role'); }
  isLoggedIn(): boolean { return !!this.getToken(); }
}