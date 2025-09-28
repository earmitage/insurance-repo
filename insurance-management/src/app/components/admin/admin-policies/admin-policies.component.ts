import { Component, OnInit } from '@angular/core';
import { NavigationComponent } from "../../../navigation/navigation.component";
import { GlobalProvider } from '../../../services/globals';
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { AdminService } from '../../../services/admin.service';
import { MatIconModule } from '@angular/material/icon';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { first } from 'rxjs/operators';
import { MatTableModule } from '@angular/material/table';
import { MatTableDataSource } from '@angular/material/table';
import { Policy } from '../../../interfaces/policy';
import { FormsModule } from '@angular/forms';
import { trigger, state, style, transition, animate } from '@angular/animations';

@Component({
  selector: 'app-admin-policies',
  standalone: true,
  imports: [CommonModule, NavigationComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    FontAwesomeModule,
    MatIconModule,
    MatTableModule,
    FormsModule
  ],
  providers: [DatePipe],
  templateUrl: './admin-policies.component.html',
  styleUrl: './admin-policies.component.scss',
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ]
})
export class AdminPoliciesComponent implements OnInit {

  loggedIn: boolean;
  columnsToDisplay = ['policyNumber', 'insuranceCompany', 'policyType', 'ownerName', 'coverageAmount', 'monthlyPremium', 'status'];
  columnsToDisplayWithExpand = [...this.columnsToDisplay, 'expand'];
  expandedElement: Policy | null;

  policies: Policy[] = [];
  dataSource = new MatTableDataSource(this.policies);

  // Search filters
  searchText: string = '';
  policyNumberFilter: string = '';
  insuranceCompanyFilter: string = '';
  ownerUsernameFilter: string = '';

  /** Checks whether an element is expanded. */
  isExpanded(element: Policy) {
    return this.expandedElement === element;
  }

  /** Toggles the expanded state of an element. */
  toggle(element: Policy) {
    this.expandedElement = this.isExpanded(element) ? null : element;
  }

  constructor(
    private router: Router,
    public global: GlobalProvider,
    private adminService: AdminService) {
    this.loggedIn = this.global.loggedIn
  }

  ngOnInit(): void {
    this.loadPolicies();
  }

  loadPolicies(searchParams?: any): void {
    this.adminService.getAllPolicies(searchParams)
      .pipe(first())
      .subscribe(
        (results: any) => {
          this.policies = results.policies || [];
          this.dataSource = new MatTableDataSource(this.policies);
        },
        (error: any) => {
          console.error('Error fetching policies:', error);
          this.global.showError('Error fetching policies', 'Close');
        }
      );
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  searchPolicies(): void {
    const searchParams: any = {};

    if (this.searchText.trim()) {
      searchParams.search = this.searchText.trim();
    }
    if (this.policyNumberFilter.trim()) {
      searchParams.policyNumber = this.policyNumberFilter.trim();
    }
    if (this.insuranceCompanyFilter.trim()) {
      searchParams.insuranceCompany = this.insuranceCompanyFilter.trim();
    }
    if (this.ownerUsernameFilter.trim()) {
      searchParams.ownerUsername = this.ownerUsernameFilter.trim();
    }

    this.loadPolicies(searchParams);
  }

  clearFilters(): void {
    this.searchText = '';
    this.policyNumberFilter = '';
    this.insuranceCompanyFilter = '';
    this.ownerUsernameFilter = '';
    this.loadPolicies();
  }

  login() {
    this.router.navigate(['/login']);
  }

  tableHeaders: Record<string, string> = {
    'policyNumber': 'Policy Number',
    'insuranceCompany': 'Insurance Company',
    'policyType': 'Policy Type',
    'ownerName': 'Owner',
    'coverageAmount': 'Coverage Amount',
    'monthlyPremium': 'Monthly Premium',
    'status': 'Status',
  };

  getOwnerName(policy: any): string {
    if (policy.owner) {
      return `${policy.owner.firstname || ''} ${policy.owner.lastname || ''}`.trim() || policy.owner.username || 'Unknown';
    }
    return 'Unknown';
  }
}