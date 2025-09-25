import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface VaultItem {
  id: string;
  title: string;
  username: string;
  url?: string;
  createdAt: string;
  updatedAt: string;
}

export interface VaultItemPayload {
  title: string;
  username: string;
  url?: string;
  secret?: string;
}

@Injectable({ providedIn: 'root' })
export class VaultService {
  private readonly baseUrl = `${environment.apiUrl}/vault`;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<VaultItem[]> {
    return this.http.get<VaultItem[]>(this.baseUrl);
  }

  create(payload: VaultItemPayload): Observable<VaultItem> {
    return this.http.post<VaultItem>(this.baseUrl, payload);
  }

  update(id: string, payload: VaultItemPayload): Observable<VaultItem> {
    return this.http.put<VaultItem>(`${this.baseUrl}/${id}`, payload);
  }

  remove(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  reveal(id: string): Observable<{ secret: string }> {
    return this.http.get<{ secret: string }>(`${this.baseUrl}/${id}/reveal`);
  }
}
