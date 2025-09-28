import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { GlobalProvider } from '../../services/globals';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { inject} from '@angular/core';
import { first } from 'rxjs/operators';
@Component({
    selector: 'app-tos-dialog',
    templateUrl: './tos.component.html',
    imports: [MatDialogModule,
        CommonModule,
        FormsModule,
        MatCardModule,
        MatButtonModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatCheckboxModule,
        FontAwesomeModule,
        MatIconModule],
})
export class TosDialogComponent {
    tos: boolean;

    readonly dialogRef = inject(MatDialogRef<TosDialogComponent>);

    constructor(private router: Router,
        private authenticationService: GlobalProvider) { }

    acceptTos() {
        if (this.tos) {
            this.dialogRef.close();
            this.authenticationService.showSuccess('Thank you for accepting the terms', 'Close');
            let regData = this.authenticationService.getRegistrationData();
            this.authenticationService.login(regData.username, regData.password)
                .pipe(first())
                .subscribe(
                    {
                        next: (data) => {
                            this.authenticationService.clearRegistrationData();
                            let role = data.roles[0].name;
                            if (role == 'ROLE_ADMIN') {
                                this.router.navigate(['/admin']);
                            } else if (role == 'ROLE_POLICY_HOLDER') {
                                this.router.navigate(['/policies']);
                            } else {
                                this.router.navigate(['/login']);
                            }

                        },
                        error: (error) => {
                            console.log(error);

                        }
                    }
                );


        }
        else {
            this.authenticationService.showError('You must acceept the terms to proceed', 'You must acceept the terms to proceed');

        }

    }
}
