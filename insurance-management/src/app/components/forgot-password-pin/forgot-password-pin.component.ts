import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { GlobalProvider } from '../../services/globals';
import { first } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { NavigationComponent } from '../../navigation/navigation.component';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
@Component({
  selector: 'app-forgot-password-pin',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, CommonModule, NavigationComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './forgot-password-pin.component.html',
  styleUrls: ['./forgot-password-pin.component.scss']
})
export class ForgotPasswordPinComponent {
  loading = false;
  submitted = false;
  error = '';
  confFailed = false;

  tokenConfirmForm: FormGroup;
  constructor(private router: Router,
    private authenticationService: GlobalProvider,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute) { }

  ngOnInit() {
    this.tokenConfirmForm = this.formBuilder.group({
      token: ['', Validators.required],
      newPassword: ['', Validators.required],
      newPasswordConfirm: ['', Validators.required],
      username: ['', Validators.required],

    });

  }
  get f() { return this.tokenConfirmForm?.controls; }

  passwordRequired() :boolean {

   let tokens :any = this.tokenConfirmForm?.controls['token'];
     if(tokens ) {
      return true;
     };
     return false;
 
  }

  login() {
    this.router.navigate(['/login']);
  }

  onPinConfirm() {
   
    this.submitted = true;
    if (this.f?.['invalid']) {
      return;
    }
    this.loading = true;
    let data = {
      token: this.tokenConfirmForm.controls['token']?.value,
      username: this.tokenConfirmForm.controls['username']?.value,
      newPassword: this.tokenConfirmForm.controls['newPassword']?.value,

    }
    this.authenticationService.resetPassword(data)
      .pipe(first())
      .subscribe(
        (data:any) => {
          this.confFailed = false;
          if (data.success) {
            this.authenticationService.showSuccess('Your password has been reset.', 'Password reset successful')
            this.authenticationService.clearRegistrationData();
            this.router.navigate(['/login']);
          }
          else {
            this.confFailed = true;
            this.error = 'Invalid OTP';
            this.loading = false;
          }
        },
        (error:any) => {
          this.confFailed = true;
          this.error = JSON.stringify(error);
          this.loading = false;

        });
  }
}
