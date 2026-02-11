import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, of, switchMap } from 'rxjs';
import { AccountInfo, Transaction } from '../models/models';
import { AuthService } from './auth.service';

interface BackendAccountResponse {
  id: number;
  holderName: string;
  balance: number;
  status: string;
}

interface BackendTransactionResponse {
  id: string; // UUID
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  status: string;
  failureReason: string;
  createdOn: string;
}

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = 'http://localhost:8080/api/v1/accounts';

  constructor() { }

  getAccountInfo(): Observable<AccountInfo> {
    const user = this.authService.getCurrentUser();
    if (!user) {
      throw new Error('User not logged in');
    }

    return this.http.get<BackendAccountResponse>(`${this.apiUrl}/${user.id}`).pipe(
      switchMap(account => {
        return this.getTransactions().pipe(
          map(transactions => ({
            user: user,
            balance: account.balance,
            transactions: transactions
          }))
        );
      })
    );
  }

  getBalance(): Observable<number> {
    const user = this.authService.getCurrentUser();
    if (!user) return of(0);
    return this.http.get<number>(`${this.apiUrl}/${user.id}/balance`);
  }

  getTransactions(): Observable<Transaction[]> {
    const user = this.authService.getCurrentUser();
    if (!user) return of([]);

    return this.http.get<BackendTransactionResponse[]>(`${this.apiUrl}/${user.id}/transactions`).pipe(
      map(backendTransactions => backendTransactions.map(t => ({
        id: t.id.toString(), // or t.id.toString() if UUID
        date: new Date(t.createdOn), // Changed from timestamp to createdOn
        type: t.fromAccountId.toString() === user.id ? 'DEBIT' : 'CREDIT', // Derive type
        amount: t.amount,
        description: t.status === 'FAILED' ? `Failed: ${t.failureReason}` : (t.fromAccountId.toString() === user.id ? `Transfer to ${t.toAccountId}` : `Received from ${t.fromAccountId}`),
        status: t.status === 'SUCCESS' ? 'SUCCESS' : 'FAILED',
        otherParty: t.fromAccountId.toString() === user.id ? t.toAccountId.toString() : t.fromAccountId.toString()
      } as Transaction)))
    );
  }
}
