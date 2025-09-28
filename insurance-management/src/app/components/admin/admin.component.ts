import { Component, OnInit } from '@angular/core';
import { NavigationComponent } from "../../navigation/navigation.component";
import { GlobalProvider } from '../../services/globals';
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { AdminService } from '../../services/admin.service';
import { MatIconModule } from '@angular/material/icon';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { first } from 'rxjs/operators';
import { MatTableModule } from '@angular/material/table';
import { MinimalUser } from '../../interfaces/minimal-user';
import { MatTableDataSource } from '@angular/material/table';
import { Policy } from '../../interfaces/policy';
import { PolicyholderService } from '../../services/policyholder.service';
import { MatDialog } from '@angular/material/dialog';
import { UpdateDeceasedDialogComponent } from './update-deceased-dialog/update-deceased-dialog.component';
import { BeneficiaryDialogComponent } from './beneficiary-dialog.component';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, NavigationComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    FontAwesomeModule,
    MatIconModule,
    MatTableModule
  ],
  providers: [DatePipe],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.scss'
})
export class AdminComponent implements OnInit {

  loggedIn: boolean;
  columnsToDisplay = ['idNumber', 'dateOfJoining', 'username', 'firstname', 'lastname', 'phone', 'email'];
  columnsToDisplayWithExpand = [...this.columnsToDisplay, 'Policies', 'expand'];
  expandedElement: MinimalUser | null;

  users: MinimalUser[] = [];

  dataSource = new MatTableDataSource(this.users);
  /** Checks whether an element is expanded. */
  isExpanded(element: MinimalUser) {
    return this.expandedElement === element;
  }

  /** Toggles the expanded state of an element. */
  toggle(element: MinimalUser) {
    this.expandedElement = this.isExpanded(element) ? null : element;
  }


  constructor(
    private router: Router,
    public global: GlobalProvider,
    private adminService: AdminService,
    public policyServices: PolicyholderService,
    private dialog: MatDialog) {
    this.loggedIn = this.global.loggedIn
  }

  ngOnInit(): void {
    this.adminService.fetchUsers()
      .pipe(first())
      .subscribe(
        (results: any) => {
          this.users = results;
          this.dataSource = new MatTableDataSource(this.users);
        }
      );
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  login() {


    this.router.navigate(['/login']);
  }
  tableHeaders: Record<string, string> = {
    'idNumber': 'ID Number',
    'dateOfJoining': 'Date of Joining',
    'username': 'Username',
    'firstname': 'First Name',
    'lastname': 'Last Name',
    'phone': 'Phone Number',
    'email': 'Email',
  };
  findUserByPolicy(uuid: string): MinimalUser | null {
    return this.users.find(user =>
      user.policies.some(policy => policy.uuid === uuid)
    ) || null;
  }
  openDeceasedDialog(policy: any) {
    let user = this.findUserByPolicy(policy.uuid);
    const dialogRef = this.dialog.open(UpdateDeceasedDialogComponent, {
      // width: '400px',
      data: { user, policy }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {

        console.log('Updating: for ', JSON.stringify(result));
        if (result.type == 'policyholder') {
          this.policyServices.deceasedPolicyHolder(result.user.username, { deceasedDate: result.dateOfDeath })
            .pipe(first())
            .subscribe(
              (results: any) => {
                this.global.showSuccess('Policyholder death updated successfully', 'Close');
              }
            );
        }
        if (result.type == 'beneficiary') {
          this.policyServices.deceasedBeneficiary(result.beneficiary.uuid, { deceasedDate: result.dateOfDeath })
            .pipe(first())
            .subscribe(
              (results: any) => {
                this.global.showSuccess('Beneficiary death updated successfully', 'Close');
              }
            );
        }

      }
    });
  }

  openBeneficiariesPopup(beneficiaries: any[]) {
    console.log('Beneficiaries: ', beneficiaries);
    this.dialog.open(BeneficiaryDialogComponent, {
      data: { beneficiaries },
      width: '600px'
    });
  }
}
