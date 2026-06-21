import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { toast } from 'ngx-sonner';

// Lucide icons
import {
  LucideWallet,
  LucideShield,
  LucideZap,
  LucideBarChart2,
  LucideUser,
  LucideLock,
  LucideAlertCircle,
  LucideEye,
  LucideEyeOff
} from '@lucide/angular';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    LucideWallet, LucideShield, LucideZap, LucideBarChart2,
    LucideUser, LucideLock, LucideAlertCircle,
    LucideEye, LucideEyeOff
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;
  showPassword = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  get usernameControl() { return this.loginForm.get('username'); }
  get passwordControl() { return this.loginForm.get('password'); }

  get usernameInvalid(): boolean {
    const ctrl = this.usernameControl;
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }

  get passwordInvalid(): boolean {
    const ctrl = this.passwordControl;
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    this.loginForm.markAllAsTouched();
    if (this.loginForm.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        toast.success('Welcome back!', { description: 'You have successfully signed in.' });
        if (this.authService.isAdmin()) {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Invalid username or password. Please try again.';
        toast.error('Sign in failed', { description: 'Check your credentials and try again.' });
      }
    });
  }
}
