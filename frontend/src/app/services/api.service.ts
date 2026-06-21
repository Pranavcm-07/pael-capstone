import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, map, switchMap, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  baseUrl = 'http://localhost:8080/api/v1';
  constructor(private http: HttpClient) { }

  getAccount(): Observable<any> {
    return this.http.get<any[]>(`${this.baseUrl}/accounts`).pipe(
      map(accounts => accounts && accounts.length > 0 ? accounts[0] : null)
    );
  }

  getBalance(): Observable<any> {
    return this.getAccount().pipe(
      map(account => {
        if (account) return { balance: account.balance };
        return { balance: 0 };
      })
    );
  }

  transfer(data: any): Observable<any> {
    const transferPayload = {
      toAccountId: Number(data.toAccount),
      amount: data.amount,
      idempotencyKey: crypto.randomUUID()
    };

    return this.getAccount().pipe(
      switchMap(account => {
        if (!account) throw new Error('No account found');
        return this.http.post(`${this.baseUrl}/transfers`, {
          ...transferPayload,
          fromAccountId: account.id
        });
      })
    );
  }

  getHistory(): Observable<any> {
    return this.getAccount().pipe(
      switchMap(account => {
        if (!account) return of([]);
        return this.http.get(`${this.baseUrl}/accounts/${account.id}/transactions`);
      })
    );
  }

  // Reward API endpoints
  getRewardSummary(): Observable<any> {
    return this.http.get(`${this.baseUrl}/rewards/summary`);
  }

  getRewardHistory(): Observable<any> {
    return this.http.get(`${this.baseUrl}/rewards/history`);
  }

  lookupAccount(accountId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/accounts/${accountId}/lookup`);
  }

  // Admin Panel APIs
  getAdminUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/users`);
  }

  getAdminAccounts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/accounts`);
  }

  getAdminTransactions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/transactions`);
  }

  getAdminStats(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/admin/stats`);
  }

  getAdminDbMetadata(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/admin/db-metadata`);
  }

  updateAccountStatus(accountId: number, status: string, reason: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/admin/accounts/${accountId}/status`, { status, reason });
  }

  updateUserRole(userId: number, role: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/admin/users/${userId}/role`, { role });
  }

  updateAccountBalance(accountId: number, balance: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/admin/accounts/${accountId}/balance`, { balance });
  }

  getAdminAudits(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/audits`);
  }

  executeAdminQuery(query: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/admin/query`, { query });
  }

  getAdminSettings(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/admin/settings`);
  }

  updateAdminSettings(settings: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/admin/settings`, settings);
  }

  addAccountMoney(accountId: number, amount: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/admin/accounts/${accountId}/add`, { amount });
  }

  deductAccountMoney(accountId: number, amount: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/admin/accounts/${accountId}/deduct`, { amount });
  }

  getAdminReports(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/admin/reports`);
  }

  getAdminRewards(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/admin/rewards`);
  }

  adjustAdminRewards(userId: number, points: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/admin/users/${userId}/adjust-rewards`, { points });
  }
}
