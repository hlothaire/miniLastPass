import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AsyncPipe, NgIf } from '@angular/common';
import { AuthStateService } from './core/auth-state.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, NgIf, AsyncPipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private readonly authState = inject(AuthStateService);
  private readonly router = inject(Router);
  readonly profile$ = this.authState.profile$;

  logout(): void {
    this.authState.logout().subscribe({
      next: () => this.router.navigate(['/login'])
    });
  }
}
