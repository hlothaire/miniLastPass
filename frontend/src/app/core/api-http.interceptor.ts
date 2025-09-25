import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthStateService } from './auth-state.service';

export const apiHttpInterceptor: HttpInterceptorFn = (req, next) => {
  const authState = inject(AuthStateService);
  const router = inject(Router);
  const cloned = req.clone({ withCredentials: true });
  return next(cloned).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authState.clear();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
