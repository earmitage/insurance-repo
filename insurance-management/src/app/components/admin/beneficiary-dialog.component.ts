import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { Component, Inject } from '@angular/core';
import { MatTableModule } from '@angular/material/table';

@Component({
    selector: 'app-beneficiary-dialog',
    templateUrl: './beneficiary-dialog.component.html',
    imports: [
        CommonModule,
        MatCardModule,
        MatDialogModule,
        MatTableModule,
    ]
})
export class BeneficiaryDialogComponent {
    displayedColumns: string[] = [
        'fullName', 'relationship', 'sharePercentage', 'idType',
        'idNumber', 'phone', 'email'
    ];
    data: any[];

    constructor(@Inject(MAT_DIALOG_DATA) public dialogData: any) {
        this.data = dialogData.beneficiaries;
    }
}