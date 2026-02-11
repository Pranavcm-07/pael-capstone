import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { User } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      this.currentUserSubject.next(JSON.parse(savedUser));
    }
  }

  login(username: string, password: string): Observable<User> {
    // Simulated login logic
    if (username === 'john_smith' && password === 'password123') {
      const user: User = {
        id: '1',
        username: 'john_smith',
        holderName: 'John Smith',
        accountNumber: 'XXXX-XXXX-1234'
      };
      localStorage.setItem('currentUser', JSON.stringify(user));
      localStorage.setItem('token', 'mock-jwt-token');
      this.currentUserSubject.next(user);
      return of(user);
    } else {
      return throwError(() => new Error('Invalid username or password'));
    }
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
}
