import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AccountService } from '../../services/account.service';
import { Transaction } from '../../models/models';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatChipsModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {
  transactions: Transaction[] = [];
  filter: 'ALL' | 'SENT' | 'RECEIVED' = 'ALL';

  constructor(private accountService: AccountService) { }

  ngOnInit(): void {
    this.accountService.getTransactions().subscribe(txs => {
      this.transactions = txs;
    });
  }

  get filteredTransactions(): Transaction[] {
    if (this.filter === 'ALL') return this.transactions;
    if (this.filter === 'SENT') return this.transactions.filter(t => t.type === 'DEBIT');
    return this.transactions.filter(t => t.type === 'CREDIT');
  }

  setFilter(filter: 'ALL' | 'SENT' | 'RECEIVED'): void {
    this.filter = filter;
  }
}
