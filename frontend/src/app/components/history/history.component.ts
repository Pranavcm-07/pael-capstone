import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { forkJoin } from 'rxjs';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

// Lucide icons
import {
  LucideSearch,
  LucideArrowUpRight,
  LucideArrowDownLeft,
  LucideHistory,
  LucideSend
} from '@lucide/angular';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    CurrencyPipe,
    DatePipe,
    FormsModule,
    // Lucide
    LucideSearch,
    LucideArrowUpRight,
    LucideArrowDownLeft,
    LucideHistory,
    LucideSend
  ],
  templateUrl: './history.component.html',
  styleUrl: './history.component.css'
})
export class HistoryComponent implements OnInit {
  transactions = signal<any[]>([]);
  filteredTransactions = signal<any[]>([]);
  currentAccountId = signal<number | null>(null);
  isLoading = signal<boolean>(true);

  // Filter values
  statusFilter = signal<string>('all'); // 'all', 'SUCCESS', 'FAILED'
  directionFilter = signal<string>('all'); // 'all', 'sent', 'received'
  searchText = signal<string>('');

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadHistory();
  }

  loadHistory() {
    this.isLoading.set(true);
    forkJoin({
      account: this.api.getAccount(),
      history: this.api.getHistory()
    }).subscribe({
      next: (result) => {
        if (result.account) {
          this.currentAccountId.set(result.account.id);
        }

        const enriched = (result.history || []).map((tx: any) => ({
          ...tx,
          direction: tx.fromAccountId === this.currentAccountId() ? 'sent' : 'received',
          otherPartyName: tx.fromAccountId === this.currentAccountId()
            ? tx.toAccountHolderName
            : tx.fromAccountHolderName,
          otherPartyId: tx.fromAccountId === this.currentAccountId()
            ? tx.toAccountId
            : tx.fromAccountId
        }));

        const sorted = enriched.sort((a: any, b: any) =>
          new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime()
        );

        this.transactions.set(sorted);
        this.applyFilters();
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  applyFilters() {
    const search = this.searchText().trim().toLowerCase();
    const status = this.statusFilter();
    const direction = this.directionFilter();

    const filtered = this.transactions().filter((tx: any) => {
      // Status filter
      if (status !== 'all' && tx.status !== status) return false;

      // Direction filter
      if (direction !== 'all' && tx.direction !== direction) return false;

      // Search term
      if (search) {
        const partyName = (tx.otherPartyName || '').toLowerCase();
        const otherId = String(tx.otherPartyId || '');
        if (!partyName.includes(search) && !otherId.includes(search)) return false;
      }

      return true;
    });

    this.filteredTransactions.set(filtered);
  }

  onSearchChange(val: string) {
    this.searchText.set(val);
    this.applyFilters();
  }

  onStatusChange(val: string) {
    this.statusFilter.set(val);
    this.applyFilters();
  }

  onDirectionChange(val: string) {
    this.directionFilter.set(val);
    this.applyFilters();
  }
}