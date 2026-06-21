import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { forkJoin } from 'rxjs';
import { RouterModule } from '@angular/router';

// Spartan imports
import { HlmCardImports } from '@spartan/ui/card';
import { HlmSkeleton } from '@spartan/ui/skeleton';
import { HlmTableImports } from '@spartan/ui/table';
import { HlmBadge } from '@spartan/ui/badge';

// Lucide icons
import {
  LucideActivity,
  LucideCheckCircle2,
  LucideXCircle,
  LucideArrowUpRight,
  LucideArrowDownLeft,
  LucideUsers
} from '@lucide/angular';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    CurrencyPipe,
    // Spartan
    HlmCardImports,
    HlmSkeleton,
    HlmTableImports,
    HlmBadge,
    // Lucide
    LucideActivity,
    LucideCheckCircle2,
    LucideXCircle,
    LucideArrowUpRight,
    LucideArrowDownLeft,
    LucideUsers
  ],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.css'
})
export class AnalyticsComponent implements OnInit {
  currentAccountId = signal<number | null>(null);
  isLoading = signal<boolean>(true);

  // KPI Metrics
  totalTransactions = signal<number>(0);
  successfulTransactions = signal<number>(0);
  failedTransactions = signal<number>(0);
  totalAmountTransferred = signal<number>(0);
  totalAmountReceived = signal<number>(0);
  mostTransactedPerson = signal<{ name: string; count: number }>({ name: '', count: 0 });
  topRecipients = signal<any[]>([]);

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadAnalytics();
  }

  loadAnalytics() {
    this.isLoading.set(true);
    forkJoin({
      account: this.api.getAccount(),
      history: this.api.getHistory()
    }).subscribe({
      next: (result) => {
        if (result.account) {
          this.currentAccountId.set(result.account.id);
        }
        this.calculateAnalytics(result.history || []);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  calculateAnalytics(transactions: any[]) {
    this.totalTransactions.set(transactions.length);

    this.successfulTransactions.set(transactions.filter(t => t.status === 'SUCCESS').length);
    this.failedTransactions.set(transactions.filter(t => t.status === 'FAILED').length);

    let sentTotal = 0;
    let receivedTotal = 0;

    transactions.forEach((tx: any) => {
      if (tx.status === 'SUCCESS') {
        if (tx.fromAccountId === this.currentAccountId()) {
          sentTotal += Number(tx.amount);
        } else {
          receivedTotal += Number(tx.amount);
        }
      }
    });

    this.totalAmountTransferred.set(sentTotal);
    this.totalAmountReceived.set(receivedTotal);

    const personCount: { [key: string]: number } = {};

    transactions.forEach((tx: any) => {
      const otherParty = tx.fromAccountId === this.currentAccountId()
        ? tx.toAccountHolderName
        : tx.fromAccountHolderName;

      if (otherParty) {
        personCount[otherParty] = (personCount[otherParty] || 0) + 1;
      }
    });

    let maxCount = 0;
    let maxPerson = '';
    for (const [person, count] of Object.entries(personCount)) {
      if (count > maxCount) {
        maxCount = count;
        maxPerson = person;
      }
    }

    this.mostTransactedPerson.set({ name: maxPerson, count: maxCount });

    const topList = Object.entries(personCount)
      .map(([person, count]) => ({
        name: person,
        count: count
      }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 5);

    this.topRecipients.set(topList);
  }
}
