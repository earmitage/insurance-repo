import { CommonModule, CurrencyPipe } from '@angular/common';
import { AfterViewInit, Component } from '@angular/core';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Policy } from '../../../interfaces/policy';
import { GlobalProvider } from '../../../services/globals';
import { PolicyholderService } from '../../../services/policyholder.service';
import { first, catchError } from 'rxjs/operators';
import { NavigationComponent } from "../../../navigation/navigation.component";
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { of } from 'rxjs';
import { PolicyService } from './policy.service';
import { Subscription } from '../../../interfaces/subscription';
import { GetPolicies } from '../../../interfaces/get-policies';
@Component({
  selector: 'app-policies',
  templateUrl: './policies.component.html',
  styleUrls: ['./policies.component.scss'],
  providers: [CurrencyPipe],
  standalone: true,
  imports: [
    CommonModule,
    MatSnackBarModule,
    NavigationComponent,
    MatTableModule,
    MatIconModule,
    MatButtonModule
  ],
})
export class AllPoliciesComponent implements AfterViewInit {
  policies: Policy[] = [];


  columnsToDisplay = ['policyType', 'policyNumber', 'insuranceCompany', 'coverageAmount', 'monthlyPremium', 'status'];
  columnsToDisplayWithExpand = [...this.columnsToDisplay, 'edit', 'expand'];
  expandedElement: Policy | null;

  /** Checks whether an element is expanded. */
  isExpanded(element: Policy) {
    return this.expandedElement === element;
  }

  /** Toggles the expanded state of an element. */
  toggle(element: Policy) {
    this.expandedElement = this.isExpanded(element) ? null : element;
  }
  subscriptions:Subscription[] = [];  


  constructor(private router: Router, public global: GlobalProvider, public policyServices: PolicyholderService, private policyService: PolicyService) {

  }

  ngAfterViewInit(): void {
    try {
      if (this.global.currentUserValue == null) {
        this.router.navigate(['/login']);
      }

      this.policyServices.fetchUserPolicies()
        .pipe(
          catchError(err => {
            console.log('Handling error locally and rethrowing it...', err);
            this.router.navigate(['/login']);
            return of([]);;
          })
        )
        .pipe(first())
        .subscribe(
          (result: GetPolicies | never[]) => {
            if (Array.isArray(result)) {
              this.policies = [];
              this.subscriptions = [];
            } else {
              this.policies = result.policies;
              this.subscriptions = result.activeSubcriptions;

              if(this.subscriptions.length > 0 && this.policies.length == 0) {
                this.global.showSuccess('Thank you for your payment subscription, you may now add policies.', 'Close');
              }
            }
          }
        ),
        (error: any) => {
          console.error('Error fetching policies:', error);
          this.router.navigate(['/login']);
        };
    }
    catch (e) {
      this.router.navigate(['/login']);
    }
  }

  addPolicy() {
    this.router.navigate(['/add-policy']);
  }

  editPolicy(policy: any): void {
    // Save the selected policy using the PolicyService
    this.policyService.setSelectedPolicy(policy);
    // Navigate to the Edit Policy Component
    this.router.navigate(['/edit-policy']);
  }

  subscribe(){
    this.router.navigate(['/my-profile']);
  }


  tableHeaders: Record<string, string> = {
    'policyNumber': 'Policy Number',
    'insuranceCompany': 'Insurance Company',
    'policyType': 'Policy Type',
    'coverageAmount': 'Coverage Amount',
    'monthlyPremium': 'Monthly Premium',
    'status': 'Status',
    'edit': 'Edit'
  };

}