/*
import { Injectable } from '@angular/core';
import { Router, CanActivate } from '@angular/router';
import { GlobalProvider } from './globals';

@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {
    constructor(
        private router: Router,
        private globals: GlobalProvider
    ) { }

    canActivate(): boolean {
        try {
            const user = this.globals.currentUserValue;
            const isAdmin = user?.roles?.some(role => role.authority === 'ROLE_ADMIN') || false;

            if (isAdmin) {
                return true;
            } else {
                this.router.navigate(['/policies']);
                return false;
            }
        } catch (error) {
            this.router.navigate(['/login']);
            return false;
        }
    }
}
    */