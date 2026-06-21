import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { RouterModule } from '@angular/router';

// Lucide icons
import {
  LucideStar,
  LucideTrophy,
  LucideSend,
  LucideDollarSign,
  LucideCheckCircle,
  LucideCalculator,
  LucideZap
} from '@lucide/angular';

@Component({
  selector: 'app-rewards',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    CurrencyPipe,
    DatePipe,
    // Lucide
    LucideStar,
    LucideTrophy,
    LucideSend,
    LucideDollarSign,
    LucideCheckCircle,
    LucideCalculator,
    LucideZap
  ],
  templateUrl: './rewards.component.html',
  styleUrl: './rewards.component.css'
})
export class RewardsComponent implements OnInit {
  totalPoints = signal<number>(0);
  rewardHistory = signal<any[]>([]);
  isLoading = signal<boolean>(true);

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadRewards();
  }

  loadRewards() {
    this.isLoading.set(true);

    // Load summary
    this.api.getRewardSummary().subscribe({
      next: (data: any) => {
        this.totalPoints.set(data.totalPoints || 0);
      },
      error: () => {
        this.totalPoints.set(0);
      }
    });

    // Load history
    this.api.getRewardHistory().subscribe({
      next: (data: any[]) => {
        const sorted = (data || []).sort((a: any, b: any) =>
          new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime()
        );
        this.rewardHistory.set(sorted);
        this.isLoading.set(false);
      },
      error: () => {
        this.rewardHistory.set([]);
        this.isLoading.set(false);
      }
    });
  }

  getShortTransactionId(id: string): string {
    if (!id) return 'N/A';
    return id.length > 8 ? id.substring(0, 8).toUpperCase() : id.toUpperCase();
  }
}
