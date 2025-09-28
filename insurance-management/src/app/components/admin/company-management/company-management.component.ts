import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { first } from 'rxjs/operators';

import { NavigationComponent } from '../../../navigation/navigation.component';
import { GlobalProvider } from '../../../services/globals';
import { CompanyService } from '../../../services/company.service';
import { Company, CreateCompanyDto, UpdateCompanyDto } from '../../../interfaces/company';

@Component({
  selector: 'app-company-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NavigationComponent,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatIconModule,
    MatDialogModule
  ],
  templateUrl: './company-management.component.html',
  styleUrl: './company-management.component.scss'
})
export class CompanyManagementComponent implements OnInit {
  companies: Company[] = [];
  dataSource = new MatTableDataSource(this.companies);
  displayedColumns: string[] = ['name', 'address', 'city', 'phoneNumber', 'email', 'actions'];

  companyForm: FormGroup;
  isEditing = false;
  editingCompanyId: string | null = null;
  showForm = false;

  constructor(
    private companyService: CompanyService,
    public global: GlobalProvider,
    private fb: FormBuilder,
    private dialog: MatDialog
  ) {
    this.companyForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      address: ['', [Validators.required]],
      city: ['', [Validators.required]],
      region: ['', [Validators.required]],
      postalCode: ['', [Validators.required]],
      country: ['', [Validators.required]],
      phoneNumber: [''],
      email: ['', [Validators.email]],
      website: [''],
      registrationNumber: ['']
    });
  }

  ngOnInit(): void {
    this.loadCompanies();
  }

  loadCompanies(): void {
    this.companyService.getCompanies()
      .pipe(first())
      .subscribe({
        next: (companies) => {
          this.companies = companies;
          this.dataSource = new MatTableDataSource(this.companies);
        },
        error: (error) => {
          this.global.showError('Failed to load companies', 'Error');
        }
      });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  showAddForm(): void {
    this.isEditing = false;
    this.editingCompanyId = null;
    this.companyForm.reset();
    this.showForm = true;
  }

  editCompany(company: Company): void {
    this.isEditing = true;
    this.editingCompanyId = company.id;
    this.companyForm.patchValue(company);
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.isEditing = false;
    this.editingCompanyId = null;
    this.companyForm.reset();
  }

  onSubmit(): void {
    if (this.companyForm.valid) {
      const formValue = this.companyForm.value;

      if (this.isEditing && this.editingCompanyId) {
        this.updateCompany(this.editingCompanyId, formValue);
      } else {
        this.createCompany(formValue);
      }
    } else {
      this.global.showError('Please fill in all required fields', 'Validation Error');
    }
  }

  private createCompany(companyData: CreateCompanyDto): void {
    this.companyService.createCompany(companyData)
      .pipe(first())
      .subscribe({
        next: (company) => {
          this.global.showSuccess('Company created successfully', 'Success');
          this.loadCompanies();
          this.cancelForm();
        },
        error: (error) => {
          this.global.showError('Failed to create company', 'Error');
        }
      });
  }

  private updateCompany(id: string, companyData: UpdateCompanyDto): void {
    this.companyService.updateCompany(id, companyData)
      .pipe(first())
      .subscribe({
        next: (company) => {
          this.global.showSuccess('Company updated successfully', 'Success');
          this.loadCompanies();
          this.cancelForm();
        },
        error: (error) => {
          this.global.showError('Failed to update company', 'Error');
        }
      });
  }

  deleteCompany(company: Company): void {
    if (confirm(`Are you sure you want to delete ${company.name}?`)) {
      this.companyService.deleteCompany(company.id)
        .pipe(first())
        .subscribe({
          next: () => {
            this.global.showSuccess('Company deleted successfully', 'Success');
            this.loadCompanies();
          },
          error: (error) => {
            this.global.showError('Failed to delete company', 'Error');
          }
        });
    }
  }

  getFieldError(fieldName: string): string {
    const field = this.companyForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName} is required`;
      }
      if (field.errors['minlength']) {
        return `${fieldName} must be at least ${field.errors['minlength'].requiredLength} characters`;
      }
      if (field.errors['email']) {
        return 'Please enter a valid email address';
      }
    }
    return '';
  }
}