import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { Beneficiary } from '../../../interfaces/beneficiary';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-update-deceased-dialog',
  standalone: true,
  templateUrl: './update-deceased-dialog.component.html',
  styleUrls: ['./update-deceased-dialog.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    MatCardModule,
    MatCheckboxModule,
  ]
})
export class UpdateDeceasedDialogComponent {
  isPolicyholderDeceased = false;
  beneficiarySearchText = ''; // This holds the search query
  selectedBeneficiary: Beneficiary | null = null; // We store the selected beneficiary object
  dateOfDeath: Date | null = null;

  constructor(
    public dialogRef: MatDialogRef<UpdateDeceasedDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  onPolicyholderToggle() {
    if (this.isPolicyholderDeceased) {
      this.selectedBeneficiary = null;
      this.beneficiarySearchText = '';
    }
  }

  // When a beneficiary is selected
  onBeneficiarySelected(ben: Beneficiary) {
    console.log('Selected Beneficiary:', ben);  // Check if we are receiving the full object
    this.selectedBeneficiary = ben;
    // Update the search text based on selected beneficiary's fullName and idNumber
    this.beneficiarySearchText = `${ben.fullName} - ${ben.idNumber}`;
    this.isPolicyholderDeceased = false;
  }

  // Filter beneficiaries based on search text
  filteredBeneficiaries() {
    const query = (this.beneficiarySearchText || '').toLowerCase();
    return this.data.policy.beneficiaries.filter((ben: any) =>
      ben.fullName.toLowerCase().includes(query) ||
      ben.surname?.toLowerCase().includes(query) ||
      ben.idNumber?.toLowerCase().includes(query)
    );
  }

  // Display name and id in the input field
  displayBeneficiary(beneficiary: Beneficiary): string {
    return beneficiary ? `${beneficiary.fullName} - ${beneficiary.idNumber}` : '';
  }

  // Check if the form can be submitted
  canSubmit() {
    return this.dateOfDeath && (this.isPolicyholderDeceased || this.selectedBeneficiary);
  }

  // Submit the form data
  submit() {
    const payload = {
      type: this.isPolicyholderDeceased ? 'policyholder' : 'beneficiary',
      user:  this.data.user,
      beneficiary: this.selectedBeneficiary,   
      dateOfDeath: this.dateOfDeath
    };

    this.dialogRef.close(payload);
  }
}
