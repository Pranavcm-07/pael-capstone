import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe, TitleCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { toast } from 'ngx-sonner';

// Lucide Icons
import {
  LucideWallet,
  LucideHistory,
  LucideDatabase,
  LucideSearch,
  LucideLock,
  LucideShield,
  LucideUsers,
  LucideActivity,
  LucideSettings,
  LucidePlay,
  LucideEdit,
  LucideAlertCircle,
  LucideStar
} from '@lucide/angular';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CurrencyPipe,
    DatePipe,
    TitleCasePipe,
    LucideWallet,
    LucideHistory,
    LucideDatabase,
    LucideSearch,
    LucideLock,
    LucideShield,
    LucideUsers,
    LucideActivity,
    LucideSettings,
    LucidePlay,
    LucideEdit,
    LucideAlertCircle,
    LucideStar
  ],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  currentTab = signal<string>('dashboard');
  isLoading = signal<boolean>(true);

  // Data signals
  users = signal<any[]>([]);
  accounts = signal<any[]>([]);
  transactions = signal<any[]>([]);
  audits = signal<any[]>([]);
  stats = signal<any>(null);
  dbMetadata = signal<any>(null);

  // New settings and reports signals
  minTransferAmount = signal<number>(1.00);
  maxTransferAmount = signal<number>(10000.00);
  transfersEnabled = signal<boolean>(true);
  reportsData = signal<any>(null);

  // Search filter inputs
  userSearch = signal<string>('');
  accountSearch = signal<string>('');
  txSearch = signal<string>('');
  auditSearch = signal<string>('');
  walletSearch = signal<string>('');

  // Transaction Filters
  txStatusFilter = signal<string>('ALL');
  txDateFilter = signal<string>('');

  // Rewards signals
  rewards = signal<any[]>([]);
  rewardsSearch = signal<string>('');
  adjustingUserRewards = signal<any | null>(null);
  rewardsAdjustmentAmount = signal<number>(0);
  adjustRewardsAction = signal<string>('add'); // 'add' or 'deduct'

  // SQL Console signals
  sqlQuery = signal<string>('SELECT id, username, role FROM users LIMIT 10');
  queryColumns = signal<string[]>([]);
  queryRows = signal<any[]>([]);
  queryError = signal<string>('');
  queryLoading = signal<boolean>(false);

  // Modal / Editing states
  editingUser = signal<any | null>(null);
  editingUserRole = signal<string>('');
  
  editingAccount = signal<any | null>(null);
  editingAccountBalance = signal<number>(0);

  // Wallet Funding Modal
  fundingAccount = signal<any | null>(null);
  fundingAction = signal<string>('add'); // 'add' or 'deduct'
  fundingAmount = signal<number>(0);

  // Transaction Detail Viewer
  selectedTransaction = signal<any | null>(null);

  // User Detail Viewer
  selectedUser = signal<any | null>(null);

  constructor(private api: ApiService) {}

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.isLoading.set(true);
    this.api.getAdminStats().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        toast.error('Failed to load system stats');
        this.isLoading.set(false);
      }
    });
    this.loadDbMetadata();
  }

  // Set active tab and fetch corresponding data
  setTab(tab: string) {
    this.currentTab.set(tab);
    this.selectedUser.set(null);
    this.selectedTransaction.set(null);

    if (tab === 'dashboard') {
      this.loadDashboardData();
    } else if (tab === 'users') {
      this.loadUsers();
    } else if (tab === 'transactions') {
      this.loadTransactions();
    } else if (tab === 'wallet') {
      this.loadAccounts();
    } else if (tab === 'audits') {
      this.loadAudits();
    } else if (tab === 'reports') {
      this.loadReports();
    } else if (tab === 'settings') {
      this.loadSettings();
    } else if (tab === 'rewards') {
      this.loadRewards();
    }
  }

  loadDbMetadata() {
    this.api.getAdminDbMetadata().subscribe({
      next: (data) => {
        this.dbMetadata.set(data);
      },
      error: (err) => {
        console.error('Failed to load database details', err);
      }
    });
  }

  loadUsers() {
    this.isLoading.set(true);
    this.api.getAdminUsers().subscribe({
      next: (data) => {
        this.users.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        toast.error('Failed to load users');
        this.isLoading.set(false);
      }
    });
  }

  loadAccounts() {
    this.isLoading.set(true);
    this.api.getAdminAccounts().subscribe({
      next: (data) => {
        this.accounts.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        toast.error('Failed to load accounts');
        this.isLoading.set(false);
      }
    });
  }

  loadTransactions() {
    this.isLoading.set(true);
    this.api.getAdminTransactions().subscribe({
      next: (data) => {
        const sorted = data.sort((a: any, b: any) => new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime());
        this.transactions.set(sorted);
        this.isLoading.set(false);
      },
      error: () => {
        toast.error('Failed to load transactions');
        this.isLoading.set(false);
      }
    });
  }

  loadSettings() {
    this.isLoading.set(true);
    this.api.getAdminSettings().subscribe({
      next: (data) => {
        this.minTransferAmount.set(data.minTransferAmount || 1.00);
        this.maxTransferAmount.set(data.maxTransferAmount || 10000.00);
        this.transfersEnabled.set(data.transfersEnabled !== false);
        this.isLoading.set(false);
      },
      error: () => {
        toast.error('Failed to load settings');
        this.isLoading.set(false);
      }
    });
  }

  saveSettings() {
    const settings = {
      minTransferAmount: this.minTransferAmount(),
      maxTransferAmount: this.maxTransferAmount(),
      transfersEnabled: this.transfersEnabled()
    };
    this.api.updateAdminSettings(settings).subscribe({
      next: () => {
        toast.success('System settings saved successfully!');
      },
      error: (err) => {
        toast.error(`Failed to save settings: ${err?.error?.message || err?.message}`);
      }
    });
  }

  loadReports() {
    this.isLoading.set(true);
    this.api.getAdminReports().subscribe({
      next: (data) => {
        this.reportsData.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        toast.error('Failed to load reports');
        this.isLoading.set(false);
      }
    });
  }

  loadAudits() {
    this.isLoading.set(true);
    this.api.getAdminAudits().subscribe({
      next: (data) => {
        this.audits.set(data || []);
        this.isLoading.set(false);
      },
      error: () => {
        toast.error('Failed to load audit logs');
        this.audits.set([]);
        this.isLoading.set(false);
      }
    });
  }

  // User details view
  viewUserDetails(user: any) {
    // Find matching account details
    this.api.getAdminAccounts().subscribe({
      next: (accounts) => {
        const userAcc = accounts.find(a => a.userId === user.id);
        this.selectedUser.set({
          ...user,
          account: userAcc || null
        });
      }
    });
  }

  closeUserDetails() {
    this.selectedUser.set(null);
  }

  // Toggle Lock/Unlock account status
  toggleLockAccount(account: any) {
    if (!account) return;
    const newStatus = account.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE';
    const actionLabel = newStatus === 'LOCKED' ? 'Lock' : 'Unlock';
    const reason = `Admin request to ${actionLabel.toLowerCase()} account`;

    this.api.updateAccountStatus(account.id, newStatus, reason).subscribe({
      next: () => {
        toast.success(`Account successfully ${newStatus.toLowerCase()}ed!`);
        if (this.currentTab() === 'wallet') {
          this.loadAccounts();
        } else if (this.selectedUser()) {
          // Refresh user details
          const u = this.selectedUser();
          this.viewUserDetails(u);
        }
      },
      error: (err) => {
        toast.error(`Failed to change account status: ${err?.error?.message || err?.message}`);
      }
    });
  }

  // Edit user role
  openEditRole(user: any) {
    this.editingUser.set(user);
    this.editingUserRole.set(user.role);
  }

  closeEditRole() {
    this.editingUser.set(null);
  }

  saveUserRole() {
    const user = this.editingUser();
    if (!user) return;
    const role = this.editingUserRole();
    
    this.api.updateUserRole(user.id, role).subscribe({
      next: () => {
        toast.success('User role updated successfully!');
        this.loadUsers();
        if (this.selectedUser() && this.selectedUser().id === user.id) {
          this.selectedUser.set({ ...this.selectedUser(), role });
        }
        this.closeEditRole();
      },
      error: (err) => {
        toast.error(`Failed to update role: ${err?.error?.message || err?.message}`);
      }
    });
  }

  // Transaction detail view
  viewTransactionDetails(tx: any) {
    this.selectedTransaction.set(tx);
  }

  closeTransactionDetails() {
    this.selectedTransaction.set(null);
  }

  // Wallet Funding Modals
  openFundingModal(account: any, action: string) {
    this.fundingAccount.set(account);
    this.fundingAction.set(action);
    this.fundingAmount.set(0);
  }

  closeFundingModal() {
    this.fundingAccount.set(null);
  }

  submitFunding() {
    const account = this.fundingAccount();
    if (!account) return;
    const amount = this.fundingAmount();
    if (amount <= 0) {
      toast.error('Amount must be positive');
      return;
    }

    const obs = this.fundingAction() === 'add'
      ? this.api.addAccountMoney(account.id, amount)
      : this.api.deductAccountMoney(account.id, amount);

    obs.subscribe({
      next: (res) => {
        toast.success(res.message || 'Transaction successfully completed!');
        this.loadAccounts();
        this.closeFundingModal();
      },
      error: (err) => {
        toast.error(`Funding operation failed: ${err?.error?.message || err?.message}`);
      }
    });
  }

  loadRewards() {
    this.isLoading.set(true);
    this.api.getAdminRewards().subscribe({
      next: (data) => {
        this.rewards.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        toast.error('Failed to load user rewards');
        this.isLoading.set(false);
      }
    });
  }

  openAdjustRewardsModal(userRewards: any, action: string) {
    this.adjustingUserRewards.set(userRewards);
    this.adjustRewardsAction.set(action);
    this.rewardsAdjustmentAmount.set(0);
  }

  closeAdjustRewardsModal() {
    this.adjustingUserRewards.set(null);
  }

  submitAdjustRewards() {
    const userRewards = this.adjustingUserRewards();
    if (!userRewards) return;
    
    let points = this.rewardsAdjustmentAmount();
    if (points <= 0) {
      toast.error('Points must be a positive integer');
      return;
    }

    if (this.adjustRewardsAction() === 'deduct') {
      points = -points;
    }

    this.api.adjustAdminRewards(userRewards.id, points).subscribe({
      next: (res) => {
        toast.success(res.message || 'Rewards adjusted successfully!');
        this.loadRewards();
        this.closeAdjustRewardsModal();
      },
      error: (err) => {
        toast.error(`Reward operation failed: ${err?.error?.message || err?.message}`);
      }
    });
  }

  // Execute SELECT queries on DB
  executeQuery() {
    const query = this.sqlQuery().trim();
    if (!query) {
      this.queryError.set('SQL query cannot be empty');
      return;
    }
    this.queryLoading.set(true);
    this.queryError.set('');
    this.queryColumns.set([]);
    this.queryRows.set([]);
    
    this.api.executeAdminQuery(query).subscribe({
      next: (res) => {
        this.queryColumns.set(res.columns || []);
        this.queryRows.set(res.rows || []);
        this.queryLoading.set(false);
        toast.success('Query executed successfully!');
      },
      error: (err) => {
        this.queryError.set(err?.error?.error || err?.message || 'Query execution failed');
        this.queryLoading.set(false);
      }
    });
  }

  // Filters computed lists
  filteredUsers = computed(() => {
    const q = this.userSearch().toLowerCase().trim();
    if (!q) return this.users();
    return this.users().filter(u => 
      u.username.toLowerCase().includes(q) || 
      u.role.toLowerCase().includes(q) ||
      String(u.id).includes(q)
    );
  });

  filteredAccounts = computed(() => {
    const q = this.accountSearch().toLowerCase().trim();
    if (!q) return this.accounts();
    return this.accounts().filter(acc => 
      acc.holderName.toLowerCase().includes(q) || 
      acc.username.toLowerCase().includes(q) || 
      String(acc.id).includes(q) ||
      acc.status.toLowerCase().includes(q)
    );
  });

  filteredWalletBalances = computed(() => {
    const q = this.walletSearch().toLowerCase().trim();
    if (!q) return this.accounts();
    return this.accounts().filter(acc => 
      acc.holderName.toLowerCase().includes(q) || 
      acc.username.toLowerCase().includes(q) || 
      String(acc.id).includes(q)
    );
  });

  filteredTransactions = computed(() => {
    const q = this.txSearch().toLowerCase().trim();
    const status = this.txStatusFilter();
    const dateStr = this.txDateFilter();
    
    let list = this.transactions();
    
    if (q) {
      list = list.filter(tx => 
        tx.id.toLowerCase().includes(q) || 
        String(tx.fromAccountId).includes(q) || 
        String(tx.toAccountId).includes(q) || 
        tx.fromAccountHolderName.toLowerCase().includes(q) ||
        tx.toAccountHolderName.toLowerCase().includes(q)
      );
    }
    
    if (status !== 'ALL') {
      list = list.filter(tx => tx.status === status);
    }
    
    if (dateStr) {
      list = list.filter(tx => {
        if (!tx.createdOn) return false;
        return tx.createdOn.startsWith(dateStr);
      });
    }
    
    return list;
  });

  filteredAudits = computed(() => {
    const q = this.auditSearch().toLowerCase().trim();
    if (!q) return this.audits();
    return this.audits().filter((a: any) =>
      String(a.accountId).includes(q) ||
      (a.changedBy || '').toLowerCase().includes(q) ||
      (a.reason || '').toLowerCase().includes(q) ||
      (a.oldStatus || '').toLowerCase().includes(q) ||
      (a.newStatus || '').toLowerCase().includes(q)
    );
  });

  filteredRewards = computed(() => {
    const q = this.rewardsSearch().toLowerCase().trim();
    if (!q) return this.rewards();
    return this.rewards().filter(r => 
      (r.username || '').toLowerCase().includes(q) ||
      (r.holderName || '').toLowerCase().includes(q) ||
      String(r.id).includes(q)
    );
  });
}
