import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PolicyService {
  private selectedPolicySource = new BehaviorSubject<any>(null); // Default value is null
  selectedPolicy$ = this.selectedPolicySource.asObservable();

  constructor() {}

  // Set the selected policy
  setSelectedPolicy(policy: any): void {
    this.selectedPolicySource.next(policy);
  }

  // Get the current selected policy
  getSelectedPolicy(): any {
    return this.selectedPolicySource.getValue();
  }
}
