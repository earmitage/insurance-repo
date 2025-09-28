import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { GlobalProvider } from '../../services/globals';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { NavigationComponent } from '../../navigation/navigation.component';
import { CommonModule } from '@angular/common';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { RouterModule } from '@angular/router';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { Image } from '../../interfaces/image';
import { Product } from '../../interfaces/product';
import { Subscription } from '../../interfaces/subscription';
import { User } from '../../interfaces/user';


@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
  imports: [
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    NavigationComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatNativeDateModule,
    MatDatepickerModule,
    MatIconModule,
    MatSelectModule
  ]
})
export class ProfileComponent implements OnInit {

  profileForm: FormGroup;
  selectedFile: File | null = null;
  minDate!: Date;
  maxDate!: Date;

  selectedFileName: string | null = null;
  selectedDocType: string | null = null;
  uploadedFiles: { file: File, type: string }[] = [];
  existingFiles: Image[] = [];

  fileTypes: string[] = ['ID Document', 'Passport Document', 'Proof of Payment'];  // Example file types (PDF, Image, Word)
  currentFileIndex: number = 0;  // To keep track of which file type is expected
  product: Product | null = null;
  subscriptions: Subscription[] = [];

  constructor(private fb: FormBuilder, private global: GlobalProvider) { }

  ngOnInit(): void {
    this.profileForm = this.fb.group({
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      phone: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      idNumber: ['', Validators.required],
      dateOfBirth: [''],
    });

    this.profileForm.patchValue(this.global.currentUserValue);


    const today = new Date();

    // Max allowed = 10 years ago (i.e. must be at least 10 years old)
    const tenYearsAgo = new Date();
    tenYearsAgo.setFullYear(today.getFullYear() - 10);

    // Min allowed = 100 years ago (i.e. max age = 100)
    const hundredYearsAgo = new Date();
    hundredYearsAgo.setFullYear(today.getFullYear() - 100);

    this.maxDate = tenYearsAgo;
    this.fetchUploadedFiles();

    this.global.fetchProducts().subscribe({
      next: (products: Product[]) => {
        this.product = products[0];
      },
      error: (err) => {
        console.error('Could not fetch products', err);
      }
    });

    this.global.fetchCurrentUser().subscribe({
      next: (user: User) => {
        this.subscriptions = user.subscriptions;
        this.global.setCurrentUser(user);

      },
      error: (err) => {
        console.error('Could not fetch subscriptions', err);
      }
    });

  }
  fetchUploadedFiles() {
    this.global.fetchUserFiles().subscribe({
      next: (files: Image[]) => {
        this.existingFiles = files;
      },
      error: (error: any) => {
        console.error('Error fetching uploaded files', error);
      }
    });
  }

  getFileType(): string {
    return this.fileTypes[this.currentFileIndex];
  }

  deleteFile(uuid: string) {
    this.global.deleteUserFile(uuid).subscribe({
      next: (files: Image[]) => {
        this.existingFiles = files;
        this.global.showSuccess('File successfully deleted', 'Close');
      },
      error: (error: any) => {
        console.error('Error fetching uploaded files', error);
      }
    });
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!this.selectedDocType) {
      alert('Please select a document type before uploading.');
      return;
    }

    if (input.files?.length) {
      const file = input.files[0];
      this.uploadedFiles.push({ file, type: this.selectedDocType });

      // Reset doc type for next selection
      this.selectedDocType = null;
    }
  }

  removeFile(index: number) {
    this.uploadedFiles.splice(index, 1);
  }


  uploadFiles() {
    const formData = new FormData();
    this.uploadedFiles.forEach((entry, index) => {
      formData.append(`file${index}`, entry.file);
      formData.append(`type${index}`, entry.type); // Include document type label
      console.log('append:', `type${index}`, entry.type);
    });

    // Assuming you have an API service to handle the file upload:
    this.global.uploadFiles(formData).subscribe({
      next: (response: any) => {
        this.global.showSuccess('Files uploaded successfully', 'Close');
        this.fetchUploadedFiles();
        this.uploadedFiles = [];
      },
      error: (error: any) => {
        this.global.showError('File upload error', 'Close');
      }
    });


  }

  onSubmit(): void {

    const raw = this.profileForm.getRawValue();
    const date = this.profileForm.get('dateOfBirth')?.value;
    const formattedDate = date instanceof Date
      ? date.toISOString().split('T')[0] // returns 'YYYY-MM-DD'
      : date;

    raw.dateOfBirth = formattedDate;


    this.global.updateProfile(raw).subscribe({
      next: (user: any) => {
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.global.setCurrentUser(user);
        this.global.showSuccess('Profile updated successfully.', 'Close');
      },
      error: (err: any) => {
        this.global.showError('Failed to update profile.', 'Close');
      }
    });
  }

  payNow() {

    let data = { amount: this.product?.annualCost, productUuid: this.product?.uuid, currency: 'ZAR' };

    this.global.initiatePayment(data)
      .subscribe({
        next: (res) => {
          if (res.redirectUrl) {
            window.location.href = res.redirectUrl; // Redirect to PayFast
          }
        },
        error: (err) => {
          console.error('Payment initiation failed', err);
          alert('Payment failed to initiate');
        }
      });
  }
}