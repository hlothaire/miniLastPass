import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthStateService } from './auth-state.service';

export const authGuard: CanActivateFn = () => {
  const authState = inject(AuthStateService);
  const router = inject(Router);
  return authState.waitForProfile().pipe(
    map(profile => {
      if (profile) {
        return true;
      }
      router.navigate(['/login']);
      return false;
    })
  );
};
