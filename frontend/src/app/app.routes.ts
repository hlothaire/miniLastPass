import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { SignupComponent } from './auth/signup/signup.component';
import { VaultListComponent } from './vault/list/vault-list.component';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'vault' },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'vault', component: VaultListComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'vault' }
];
