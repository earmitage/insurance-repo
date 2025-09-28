import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Validators } from 'ngx-editor';
import { first } from 'rxjs/operators';
import { GlobalProvider } from '../../services/globals';
import { NavigationComponent } from '../../navigation/navigation.component';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    NavigationComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,

  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {


  loginformD = true;
  recoverform = false;
  loading = false;
  submitted = false;
  loginFailed = false;
  returnUrl: string;
  error: string;
  loginForm: FormGroup;
  forgotPasswordEmail: string;

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private authenticationService: GlobalProvider
  ) {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

  }

  ngOnInit() {

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  showRecoverForm() {
    this.loginformD = !this.loginformD;
    this.recoverform = !this.recoverform;
  }
  get f() { return this.loginForm.controls; }

  onLogin() {
    console.log('LOGIN');
    this.submitted = true;
    if (this.loginForm.invalid) {
      return;
    }
    this.loading = true;
    this.error = '';
    this.loginFailed = false;
     console.log('SERVICE');
    this.authenticationService.login(this.loginForm.controls['username']?.value, this.loginForm.controls['password']?.value)
      .pipe(first())
      .subscribe(
        {
          next: (data) => {
           console.log(data);
           let role= data.roles[0];
            if (role == 'ROLE_ADMIN') {
              console.log('navigating to admin');
              this.router.navigate(['/admin']);
            } else if (role == 'ROLE_POLICY_HOLDER') {
              this.router.navigate(['/policies']);
            } else {
              this.router.navigate(['/login']);
            } 

          },
          error: (error) => {
            console.log('here is the error');
            console.log('erro is '+JSON.stringify( error));
            this.error = error.error ?? error.error | error;
            this.loginFailed = true;
            if (error.status == 403 || error.status == 401) {
              this.error = 'Invalid username or password';
              this.authenticationService.showError(this.error, 'Close');
            } else {
              this.error = error.error ?? error.error | error;
            }

            this.loading = false;
          }
        }
      );
  }

  register() {
    this.router.navigate(['/register']);
  }

  forgotPassword() {
    this.router.navigate(['/forgot-password']);
  }

  onForgotPassword() {
    this.authenticationService.forgotPassword({ username: this.forgotPasswordEmail })
      .pipe(first())
      .subscribe(
        {
          next: (data: any) => {
            this.router.navigate(['/forgot-password-pin']);
          },
          error: (error: any) => {
            this.router.navigate(['/forgot-password-pin']);
          }
        }
      );
  }
}
