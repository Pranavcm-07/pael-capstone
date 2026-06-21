import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Router, RouterModule } from '@angular/router';



// Lucide icons
import {
  LucideSend,
  LucideHistory,
  LucideBarChart2,
  LucideStar,
  LucideEye,
  LucideEyeOff,
  LucideArrowUpRight,
  LucideArrowDownLeft
} from '@lucide/angular';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    CurrencyPipe,
    // Lucide
    LucideSend,
    LucideHistory,
    LucideBarChart2,
    LucideStar,
    LucideEye,
    LucideEyeOff,
    LucideArrowUpRight,
    LucideArrowDownLeft
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  balance = signal<number>(0);
  balanceVisible = signal<boolean>(true);
  totalRewardPoints = signal<number>(0);
  recentTransactions = signal<any[]>([]);
  isLoading = signal<boolean>(true);
  accountId = signal<number | null>(null);
  holderName = signal<string>('User');

  constructor(
    private api: ApiService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.isLoading.set(true);
    // Fetch account details, balance, and reward points in parallel-ish subscription
    this.api.getAccount().subscribe({
      next: (account: any) => {
        if (account) {
          this.balance.set(account.balance || 0);
          this.accountId.set(account.id);
          this.holderName.set(account.holderName || account.username || 'User');
          this.loadRecentTransactions();
        } else {
          this.isLoading.set(false);
        }
      },
      error: () => {
        this.isLoading.set(false);
      }
    });

    this.api.getRewardSummary().subscribe({
      next: (data: any) => {
        this.totalRewardPoints.set(data.totalPoints || 0);
      },
      error: () => {
        this.totalRewardPoints.set(0);
      }
    });
  }

  loadRecentTransactions() {
    this.api.getHistory().subscribe({
      next: (transactions: any[]) => {
        if (transactions && Array.isArray(transactions)) {
          const currentId = this.accountId();
          const enriched = transactions.map((tx: any) => ({
            ...tx,
            direction: tx.fromAccountId === currentId ? 'sent' : 'received',
            otherPartyName: tx.fromAccountId === currentId 
              ? tx.toAccountHolderName 
              : tx.fromAccountHolderName,
          }));
          const sorted = enriched.sort((a, b) => {
            return new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime();
          });
          this.recentTransactions.set(sorted.slice(0, 5));
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  toggleBalanceVisibility() {
    this.balanceVisible.update(v => !v);
  }

  isSentTransaction(tx: any): boolean {
    return tx.fromAccountId === this.accountId();
  }
}
