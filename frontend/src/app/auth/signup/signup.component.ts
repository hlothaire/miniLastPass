import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { AuthStateService } from '../../core/auth-state.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.scss'
})
export class SignupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authState = inject(AuthStateService);
  private readonly router = inject(Router);

  error: string | null = null;

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(12)]],
    confirmPassword: ['', [Validators.required]]
  });

  submit(): void {
    if (this.form.invalid || this.form.value.password !== this.form.value.confirmPassword) {
      this.error = 'Passwords must match and meet requirements.';
      this.form.markAllAsTouched();
      return;
    }
    const { email, password } = this.form.getRawValue();
    this.authState
      .signup({ email, password })
      .pipe(switchMap(() => this.authState.login({ email, password })))
      .subscribe({
        next: () => this.router.navigate(['/vault']),
        error: err => (this.error = err.error?.error ?? 'Sign up failed')
      });
  }
}
