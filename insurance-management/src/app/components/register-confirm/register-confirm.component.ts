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
  selector: 'app-dashboard',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, CommonModule,
    NavigationComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  
  ],
  templateUrl: './register-confirm.component.html',
  styleUrls: ['./register-confirm.component.scss']
})
export class RegisterConfirmComponent {
  registrationConfirmForm!: FormGroup;
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
    this.registrationConfirmForm = this.formBuilder.group({
      token: ['', Validators.required]

    });

    // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }
  get f() { return this.registrationConfirmForm?.controls; }

  passwordRequired() :boolean {

   let tokens :any = this.registrationConfirmForm?.controls['token'];
     if(tokens ) {
      return true;
     };
     return false;
 
  }

  login() {
    this.router.navigate(['/login']);
  }

  onRegister() {
    let regData = this.authenticationService.getRegistrationData();
    let request = { 'username': regData.username, 'token': this.f?.['token'].value, 'password': regData.password};
    this.submitted = true;
    if (this.f?.['invalid']) {
      return;
    }
    this.loading = true;
    this.authenticationService.confirmRegistration(request)
      .pipe(first())
      .subscribe(
        (data:any) => {
          this.confFailed = false;
          if (data.success) {
            this.authenticationService.showSuccess('Registration successful', 'Registration successful')
            this.dialog.open(TosDialogComponent);
          
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
          this.loading = false;

        });
  }
}
