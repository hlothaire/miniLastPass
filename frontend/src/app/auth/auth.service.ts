import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthProfile } from '../core/auth-state.service';

export interface SignupRequest {
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  constructor(private readonly http: HttpClient) {}

  signup(request: SignupRequest): Observable<AuthProfile> {
    return this.http.post<AuthProfile>(`${this.baseUrl}/signup`, request);
  }

  login(request: LoginRequest): Observable<AuthProfile> {
    return this.http.post<AuthProfile>(`${this.baseUrl}/login`, request);
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {});
  }

  me(): Observable<AuthProfile> {
    return this.http.get<AuthProfile>(`${this.baseUrl}/me`);
  }
}
