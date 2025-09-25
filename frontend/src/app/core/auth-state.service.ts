import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, filter, take, tap } from 'rxjs';
import { AuthService, LoginRequest, SignupRequest } from '../auth/auth.service';

export interface AuthProfile {
  id: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly profileSubject = new BehaviorSubject<AuthProfile | null>(null);
  readonly profile$ = this.profileSubject.asObservable();
  private loaded = false;

  constructor(private readonly authService: AuthService) {
    this.authService.me().subscribe({
      next: profile => {
        this.profileSubject.next(profile);
        this.loaded = true;
      },
      error: () => {
        this.profileSubject.next(null);
        this.loaded = true;
      }
    });
  }

  waitForProfile(): Observable<AuthProfile | null> {
    if (this.loaded) {
      return this.profile$.pipe(take(1));
    }
    return this.profile$.pipe(filter(() => this.loaded), take(1));
  }

  login(request: LoginRequest): Observable<AuthProfile> {
    return this.authService.login(request).pipe(tap(profile => {
      this.loaded = true;
      this.profileSubject.next(profile);
    }));
  }

  signup(request: SignupRequest): Observable<AuthProfile> {
    return this.authService.signup(request);
  }

  logout(): Observable<void> {
    return this.authService.logout().pipe(tap(() => {
      this.loaded = true;
      this.profileSubject.next(null);
    }));
  }

  clear(): void {
    this.loaded = true;
    this.profileSubject.next(null);
  }
}
