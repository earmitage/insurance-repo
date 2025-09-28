import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, share } from 'rxjs/operators';
import { environment } from '../../environments/environment';   
import { Policy } from '../interfaces/policy';
import { GlobalProvider } from './globals';
import { GetPolicies } from '../interfaces/get-policies';

@Injectable({
  providedIn: 'root'
})
export class PolicyholderService {


  fetchUserPolicies() :Observable<GetPolicies> {
    return this.http.get<GetPolicies>(`${this.global.baseUrl}/policy-holders/${this.global.currentUserValue.username}/policies/`)
    .pipe(map(response => {
        return response;
    }));
  }

  options = { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) };
  constructor(private http: HttpClient, private global: GlobalProvider) {

  }

  createPolicy(policy: Policy): Observable<any> {
    return this.http.post<any>(`${this.global.baseUrl}/policy-holders/${this.global.currentUserValue.username}/policies/`, policy, this.options)
    .pipe(map(response => {
        return response;
    }));
  }
  
  updatePolicy(policy: Policy, uuid: string): Observable<any> {
    return this.http.put<any>(`${this.global.baseUrl}/policy-holders/${this.global.currentUserValue.username}/policies/${uuid}/`, policy, this.options)
    .pipe(map(response => {
        return response;
    }));
  }

  deceasedBeneficiary(beneficiaryUuid: string, details: any): Observable<any> {
    return this.http.put<any>(`${this.global.baseUrl}/beneficiaries/deceased/${beneficiaryUuid}/`, details, this.options)
    .pipe(map(response => {
        return response;
    }));
  }

  deceasedPolicyHolder(username: string, details: any): Observable<any> {
    return this.http.put<any>(`${this.global.baseUrl}/policy-holders/${username}/deceased/`, details, this.options)
    .pipe(map(response => {
        return response;
    }));
  }

  policyTypes: Record<string, string> = {
		'LIFE_INSURANCE': 'Life Insurance',
		'HEALTH_INSURANCE': 'Health Insurance',
		'AUTO_INSURANCE': 'Auto Insurance',
		'HOME_INSURANCE': 'Home Insurance',
		'TRAVEL_INSURANCE': 'Travel Insurance',
		'DISABILITY_INSURANCE': 'Disability Insurance',
		'TERM_LIFE': 'Term Life',
		'WHOLE_LIFE': 'Whole Life',
		'UNIVERSAL_LIFE': 'Universal Life',
	};
	policyType(value: string): string {
		return this.policyTypes[value];
	}


	policyTypeKeys() {
		return Object.keys(this.policyTypes);
	}


}
