import { Component, OnInit, signal, HostListener } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';
import { ApiService } from './services/api.service';
import { NgxSonnerToaster } from 'ngx-sonner';
import { filter } from 'rxjs';

// Lucide icons
import {
  LucideLayoutDashboard,
  LucideArrowLeftRight,
  LucideHistory,
  LucideBarChart2,
  LucideStar,
  LucideLogOut,
  LucideMenu,
  LucideX,
  LucideChevronDown,
  LucideWallet,
  LucideShield
} from '@lucide/angular';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    CommonModule,
    NgxSonnerToaster,
    // Lucide icons
    LucideLayoutDashboard,
    LucideArrowLeftRight,
    LucideHistory,
    LucideBarChart2,
    LucideStar,
    LucideLogOut,
    LucideMenu,
    LucideX,
    LucideChevronDown,
    LucideWallet,
    LucideShield
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.css']
})
export class AppComponent implements OnInit {
  username = signal<string>('');
  currentRoute = signal<string>('');
  sidebarOpen = signal<boolean>(false);
  profileOpen = signal<boolean>(false);

  readonly navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: 'layout-dashboard' },
    { path: '/transfer', label: 'Send Money', icon: 'arrow-left-right' },
    { path: '/history', label: 'History', icon: 'history' },
    { path: '/analytics', label: 'Analytics', icon: 'bar-chart-2' },
    { path: '/rewards', label: 'Rewards', icon: 'star' },
  ];

  constructor(
    public authService: AuthService,
    private api: ApiService,
    private router: Router
  ) {}

  ngOnInit() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.currentRoute.set(event.urlAfterRedirects || event.url);
      this.sidebarOpen.set(false);
      this.profileOpen.set(false);
      if (this.authService.isLoggedIn() && !this.username()) {
        this.loadUsername();
      }
    });

    if (this.authService.isLoggedIn()) {
      this.loadUsername();
    }
  }

  loadUsername() {
    this.api.getAccount().subscribe({
      next: (account: any) => {
        if (account) {
          this.username.set(account.holderName || account.username || 'User');
        }
      },
      error: () => this.username.set('User')
    });
  }

  get userInitials(): string {
    const name = this.username();
    if (!name) return 'U';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return name.substring(0, 2).toUpperCase();
  }

  get pageTitle(): string {
    const route = this.currentRoute();
    if (route.startsWith('/dashboard')) return 'Dashboard';
    if (route.startsWith('/transfer')) return 'Send Money';
    if (route.startsWith('/history')) return 'Transaction History';
    if (route.startsWith('/analytics')) return 'Analytics';
    if (route.startsWith('/rewards')) return 'Rewards';
    if (route.startsWith('/admin')) return 'Admin Panel';
    return 'TRANSFO';
  }

  get isAuthPage(): boolean {
    const route = this.currentRoute();
    return route === '/login' || route === '/register';
  }

  toggleSidebar() {
    this.sidebarOpen.update(v => !v);
  }

  closeSidebar() {
    this.sidebarOpen.set(false);
  }

  toggleProfile(event: Event) {
    event.stopPropagation();
    this.profileOpen.update(v => !v);
  }

  @HostListener('document:click')
  onDocumentClick() {
    this.profileOpen.set(false);
  }

  @HostListener('window:storage', ['$event'])
  onStorageChange(event: StorageEvent) {
    if (event.key === 'auth_token') {
      window.location.reload();
    }
  }

  logout() {
    this.username.set('');
    this.profileOpen.set(false);
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
