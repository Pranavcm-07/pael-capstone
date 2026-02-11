import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Transaction } from '../models/models';
import { AuthService } from './auth.service';

interface TransferRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  idempotencyKey?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TransferService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = 'http://localhost:8080/api/v1/transfers';

  constructor() { }

  transfer(toAccountId: string, amount: number, remarks: string = ''): Observable<Transaction> {
    const user = this.authService.getCurrentUser();
    if (!user) {
      throw new Error('User not logged in');
    }

    const request: TransferRequest = {
      fromAccountId: Number(user.id),
      toAccountId: Number(toAccountId),
      amount: amount,
      idempotencyKey: crypto.randomUUID() // Generate a random key for idempotency
    };

    return this.http.post<any>(this.apiUrl, request).pipe(
      map(response => {
        // backend returns TransferResponse which might not map 1:1 to Transaction
        // but for now let's construct a Transaction object from it or just minimal info
        return {
          id: response.transactionId,
          date: new Date(), // Backend might send date, or we use current
          type: 'DEBIT',
          amount: amount,
          description: remarks || `Transfer to ${toAccountId}`,
          status: 'SUCCESS',
          otherParty: toAccountId
        } as Transaction;
      })
    );
  }
}
