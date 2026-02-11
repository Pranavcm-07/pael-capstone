import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';
import { AccountService } from './account.service';
import { Transaction } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class TransferService {
  constructor(private accountService: AccountService) { }

  transfer(toAccountId: string, amount: number, remarks: string = ''): Observable<Transaction> {
    // Simulated API call with delay
    const newTransaction: Transaction = {
      id: Math.random().toString(36).substring(7),
      date: new Date(),
      type: 'DEBIT',
      amount: amount,
      description: remarks || `Transfer to ${toAccountId}`,
      status: 'SUCCESS',
      otherParty: toAccountId
    };

    // Update account service state
    this.accountService.updateBalance(amount, 'DEBIT');
    this.accountService.addTransaction(newTransaction);

    return of(newTransaction).pipe(delay(1000));
  }
}
