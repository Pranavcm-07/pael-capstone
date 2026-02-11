import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AccountService } from '../../services/account.service';
import { TransferService } from '../../services/transfer.service';
import { AccountInfo } from '../../models/models';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './transfer.component.html',
  styleUrls: ['./transfer.component.css']
})
export class TransferComponent implements OnInit {
  transferForm: FormGroup;
  accountInfo: AccountInfo | null = null;
  loading = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private transferService: TransferService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.transferForm = this.fb.group({
      toAccount: ['', [Validators.required]],
      amount: ['', [Validators.required, Validators.min(1)]],
      remarks: ['']
    });
  }

  ngOnInit(): void {
    this.accountService.getAccountInfo().subscribe(info => {
      this.accountInfo = info;
    });
  }

  onSubmit(): void {
    if (this.transferForm.valid && this.accountInfo) {
      if (this.transferForm.value.amount > this.accountInfo.balance) {
        this.errorMessage = 'Insufficient balance';
        return;
      }

      this.loading = true;
      this.errorMessage = null;
      this.successMessage = null;

      const { toAccount, amount, remarks } = this.transferForm.value;

      this.transferService.transfer(toAccount, amount, remarks).subscribe({
        next: () => {
          this.loading = false;
          this.successMessage = 'Transfer Successful!';
          this.snackBar.open('Transfer Complete!', 'Close', { duration: 3000 });
          setTimeout(() => this.router.navigate(['/dashboard']), 2000);
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage = 'Transfer failed. Please try again.';
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }
}
