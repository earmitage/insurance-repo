import { inject, Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpInterceptorFn, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { GlobalProvider } from './globals';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
/*
@Injectable()
export class JwtInterceptor implements HttpInterceptor {

    constructor(private authenticationService: GlobalProvider) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // add auth header with jwt if user is logged in and request is to api url
        const currentUser = this.authenticationService.currentUserValue;
        const isLoggedIn = currentUser && currentUser.token;
        console.log("currentUser", JSON.stringify(currentUser));
        const isApiUrl = request.url.startsWith(this.authenticationService.baseUrl);
        if (isLoggedIn && isApiUrl) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${currentUser.token}`,
                    actionedBy: `${this.authenticationService.currentUserValue.username}`
                }
            });
        }

        return next(request);
    }
}
    

*/
export const authInterceptor: HttpInterceptorFn = (
    request: HttpRequest<any>,
    next: HttpHandlerFn
): Observable<HttpEvent<any>> => {
    if (request.url.includes('/unsecured/')) {

    } else {
        const authenticationService = inject(GlobalProvider);
        const currentUser = authenticationService.currentUserValue;
        const isLoggedIn = currentUser && currentUser.token;
        const isApiUrl = request.url.startsWith(authenticationService.baseUrl);


        if (isLoggedIn && isApiUrl) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${currentUser.token}`,
                    actionedBy: `${authenticationService.currentUserValue.username}`
                }
            });
        }
    }

    return next(request);
}




