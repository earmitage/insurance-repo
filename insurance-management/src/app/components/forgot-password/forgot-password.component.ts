import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { GlobalProvider } from '../../services/globals';
import { first } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { NavigationComponent } from '../../navigation/navigation.component';
import { MatDialog } from '@angular/material/dialog';
import { TosDialogComponent } from './tos.component';
@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, CommonModule,
    NavigationComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  
  ],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent {
  forgotPasswordForm!: FormGroup;
  loading = false;
  submitted = false;
  returnUrl: string | undefined;
  error = '';
  confFailed = false;

  constructor(private router: Router,
    private authenticationService: GlobalProvider,
    private formBuilder: FormBuilder,private dialog: MatDialog,
    private route: ActivatedRoute) { }

  ngOnInit() {
    this.forgotPasswordForm = this.formBuilder.group({
      idNumber: ['', Validators.required],
      username: ['', Validators.required]

    });

    // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }
  get f() { return this.forgotPasswordForm?.controls; }



  login() {
    this.router.navigate(['/login']);
  }

  onReset() {
    this.submitted = true;
    if (this.f?.['invalid']) {
      return;
    }
    this.loading = true;
    this.authenticationService.forgotPassword(this.forgotPasswordForm.value)
      .pipe(first())
      .subscribe(
        (data:any) => {
          this.confFailed = false;
          if (data.success) {
            this.router.navigate(['/forgot-password-pin']);
          
          }
          else {
            this.confFailed = true;
            this.error = 'Invalid OTP';
            this.loading = false;
          }
        },
        (error:any) => {
          this.confFailed = true;
          this.error = error.error;
          this.authenticationService.showError(error.error, 'Close');
          this.loading = false;

        });
  }
}
