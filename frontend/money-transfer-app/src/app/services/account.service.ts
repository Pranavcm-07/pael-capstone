import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { AccountInfo, Transaction } from '../models/models';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private mockTransactions: Transaction[] = [
    {
      id: '1',
      date: new Date('2026-01-08T10:30:00'),
      type: 'DEBIT',
      amount: 500,
      description: 'Transfer to Jane Doe',
      status: 'SUCCESS',
      otherParty: 'Jane Doe'
    },
    {
      id: '2',
      date: new Date('2026-01-07T15:15:00'),
      type: 'CREDIT',
      amount: 1200,
      description: 'Received from Bob Wilson',
      status: 'SUCCESS',
      otherParty: 'Bob Wilson'
    },
    {
      id: '3',
      date: new Date('2026-01-06T09:45:00'),
      type: 'DEBIT',
      amount: 750,
      description: 'Transfer to Alice Brown',
      status: 'SUCCESS',
      otherParty: 'Alice Brown'
    },
    {
      id: '4',
      date: new Date('2026-01-05T11:20:00'),
      type: 'CREDIT',
      amount: 2500,
      description: 'Received from Mike Chen',
      status: 'SUCCESS',
      otherParty: 'Mike Chen'
    }
  ];

  private currentBalance = 45250;

  constructor(private authService: AuthService) { }

  getAccountInfo(): Observable<AccountInfo> {
    const user = this.authService.getCurrentUser();
    if (!user) {
      throw new Error('User not logged in');
    }
    return of({
      user: user,
      balance: this.currentBalance,
      transactions: [...this.mockTransactions].sort((a, b) => b.date.getTime() - a.date.getTime())
    });
  }

  getBalance(): Observable<number> {
    return of(this.currentBalance);
  }

  getTransactions(): Observable<Transaction[]> {
    return of([...this.mockTransactions].sort((a, b) => b.date.getTime() - a.date.getTime()));
  }

  updateBalance(amount: number, type: 'DEBIT' | 'CREDIT'): void {
    if (type === 'DEBIT') {
      this.currentBalance -= amount;
    } else {
      this.currentBalance += amount;
    }
  }

  addTransaction(transaction: Transaction): void {
    this.mockTransactions.unshift(transaction);
  }
}
