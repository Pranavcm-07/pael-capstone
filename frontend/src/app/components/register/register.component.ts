import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { toast } from 'ngx-sonner';

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
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterModule,
    LucideWallet, LucideShield, LucideZap, LucideBarChart2,
    LucideUser, LucideLock, LucideAlertCircle,
    LucideEye, LucideEyeOff
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading = false;
  showPassword = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  get usernameControl() { return this.registerForm.get('username'); }
  get passwordControl() { return this.registerForm.get('password'); }

  get usernameInvalid(): boolean {
    const ctrl = this.usernameControl;
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }

  get passwordInvalid(): boolean {
    const ctrl = this.passwordControl;
    return !!(ctrl && ctrl.invalid && ctrl.touched);
  }

  get passwordStrength(): 'weak' | 'medium' | 'strong' {
    const val = this.passwordControl?.value || '';
    if (val.length === 0) return 'weak';
    if (val.length < 6) return 'weak';
    if (val.length < 10) return 'medium';
    return 'strong';
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    this.registerForm.markAllAsTouched();
    if (this.registerForm.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        toast.success('Account created!', { description: 'Please sign in with your new account.' });
        this.router.navigate(['/login']);
      },
      error: (err: any) => {
        this.isLoading = false;
        if (err.error && typeof err.error === 'string') {
          this.errorMessage = err.error;
        } else if (err.error?.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'Registration failed. Please try again.';
        }
        toast.error('Registration failed', { description: this.errorMessage });
      }
    });
  }
}
