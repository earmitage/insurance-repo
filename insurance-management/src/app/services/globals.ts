import { Injectable, inject } from '@angular/core';
import { User } from '../interfaces/user';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { first } from 'rxjs/operators';

import { COLORS } from './graph-colors';
import { PolicyholderService } from './policyholder.service';
import { SnackBarService } from '../components/snackbar/snackbar.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Image } from '../interfaces/image';
import { Product } from '../interfaces/product';
import { Subscription } from '../interfaces/subscription';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';

@Injectable({
	providedIn: 'root',
	deps: [PolicyholderService]
})
export class GlobalProvider {

	baseUrl: string;
	role: string = '';
	static instance: GlobalProvider;
	private currentUserSubject: BehaviorSubject<User | null>;

	public currentUser: Observable<User>;
	headers = new HttpHeaders({ 'Content-Type': 'application/json' });

	options = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };
	username: any;

	tooltipShowDelay: number = 200;
	tooltipHideDelay: number = 500;
	loggedIn: boolean = false;

	private _snackBar = inject(MatSnackBar);

	constructor(private http: HttpClient, private snackBarService: SnackBarService, private router: Router) {
		let user: string = localStorage.getItem('currentUser')!;
		this.currentUserSubject = new BehaviorSubject<User | null>(JSON.parse(user));
		this.currentUser = this.currentUserSubject.asObservable().pipe(
			filter((user): user is User => user !== null)
		);
		this.baseUrl = environment.baseUrl;
		console.log('using this.baseUrl  ' + this.baseUrl);
		return GlobalProvider.instance = GlobalProvider.instance || this;
	}


	formatter = new Intl.NumberFormat('en-ZA', {
		style: 'currency',
		currency: 'ZAR',
	});


	formatMoney(money: any) {
		return this.formatter.format(money);
	}


	showSuccess(text: string, description: string) {
		this.snackBarService.openSnackBar(text, description);
		this._snackBar.open(text, 'Close', {
			duration: 5000,
			panelClass: ['custom-snackbar']
		});
		//this.toastr.success(text, description), { timeOut: 5000 };
	}

	showError(text: string, description: string) {
		this._snackBar.open(text, 'Close', {
			duration: 5000,
			panelClass: ['custom-snackbar']
		});
		//this.toastr.error(text, description), { timeOut: 5000 };
	}

	public showSuccessSnackBar(message: string) {
		this.snackBarService.openSnackBar(message, 'Close');
	}

	setRegistrationData(data: any) {
		localStorage.setItem('insurance.username', data.username);
		localStorage.setItem('insurance.password', data.password);
	}

	getRegistrationData(): any {
		return {
			'username': localStorage.getItem('insurance.username'),
			'password': localStorage.getItem('insurance.password')
		};
	}



	clearRegistrationData() {
		localStorage.removeItem('insurance.username');
		localStorage.removeItem('insurance.password');
	}

	public get currentUserValue(): User {
		if (this.currentUserSubject.value === null) {
			//throw new Error('Current user is null');
			//this.router.navigate(['/login']);
		}
		return this.currentUserSubject.value!;
	}

	setCurrentUser(user: User) {
		this.currentUserSubject.next(user);
		this.username = user.username;
		this.role = user.roles[0];
	}

	public currentYear(): number {
		return new Date().getFullYear();
	}

	login(username: string, password: string) {
		return this.http.post<any>(`${this.baseUrl}/unsecured/auth/`, { username, password }, this.options)
			.pipe(map(user => {
				// login successful if there's a jwt token in the response
				if (user && user.token) {
					// store user details and jwt token in local storage to keep user logged in between page refreshes
					localStorage.setItem('currentUser', JSON.stringify(user));
					this.currentUserSubject.next(user);
					this.loggedIn = true;
				}

				return user;
			}));
	}

	fetchCurrentUser(): Observable<User> {
		const url = `${this.baseUrl}/secured/users/${this.currentUserValue.username}/`;
		return this.http.get<User>(url).pipe(first());
	}

	register(data: any): any {
		return this.http.post<any>(`${this.baseUrl}/unsecured/registrations/`, data, this.options);
	}

	forgotPassword(data: any): any {
		return this.http.post<any>(`${this.baseUrl}/unsecured/resetPassword/`, data, this.options)
			.pipe(map(response => {
				return response;
			}));
	}

	resetPassword(data: any): any {
		return this.http.post<any>(`${this.baseUrl}/unsecured/users/password-reset-finalize/`, data, this.options)
			.pipe(map(response => {
				return response;
			}));
	}

	confirmRegistration(data: any): any {
		return this.http.post<any>(`${this.baseUrl}/unsecured/registration-confirmations/`, data, this.options)
			.pipe(map(response => {
				return response;
			}));
	}

	fetchInvite(inviteUuid: string): any {
		return this.http.get<any>(`${this.baseUrl}/unsecured/bank-invitations/${inviteUuid}`)
			.pipe(first());
	}

	acceptInvite(inviteUuid: string, data: any): any {
		return this.http.put<any>(`${this.baseUrl}/unsecured/bank-invitations/${inviteUuid}`, data, this.options)
			.pipe(first());
	}

	updateProfile(user: any): any {
		return this.http.put<any>(`${this.baseUrl}/secured/users/${this.currentUserValue.username}/`, user, this.options)
			.pipe(first());
	}

	uploadFiles(formData: FormData): Observable<any> {
		const url = `${this.baseUrl}/secured/users/${this.currentUserValue.username}/files/`;

		return this.http.post(url, formData).pipe(first());
	}

	fetchUserFiles(): Observable<Image[]> {
		const url = `${this.baseUrl}/secured/users/${this.currentUserValue.username}/files/`;
		return this.http.get<Image[]>(url).pipe(first());
	}

	fetchUserSubscriptions(): Observable<Subscription[]> {
		const url = `${this.baseUrl}/subscriptions/user/${this.currentUserValue.username}/`;
		return this.http.get<Subscription[]>(url).pipe(first());
	}

	deleteUserFile(uuid: string): Observable<Image[]> {
		const url = `${this.baseUrl}/secured/users/${this.currentUserValue.username}/files/${uuid}/`;
		return this.http.delete<Image[]>(url).pipe(first());
	}

	initiatePayment(product: any) {
		return this.http.post<{ redirectUrl: string }>(`${this.baseUrl}/payments/initiations/`, product);
	}

	fetchProducts(): Observable<Product[]> {
		return this.http.get<Product[]>(`${this.baseUrl}/products/`).pipe(first());
	}

	logout() {
		// remove user from local storage to log user out
		localStorage.removeItem('currentUser');
		this.currentUser = new Observable<User>();
		this.username = null;
		this.currentUserSubject.next(null);
		this.loggedIn = false;
	}

	getRandomColor() {
		return COLORS[this.generateRandomInteger(1, COLORS.length)];
	}


	generateRandomInteger(min: number, max: number) {
		return Math.floor(min + Math.random() * (max - min + 1))
	}

}


