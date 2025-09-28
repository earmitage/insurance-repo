import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { NgSelectModule } from '@ng-select/ng-select';
import { first } from 'rxjs/operators';
import { GlobalProvider } from '../../services/globals';
import { NavigationComponent } from "../../navigation/navigation.component";
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { AFRICAN_COUNTRY_CODES, Country } from '../../interfaces/country';
import { MatSelectModule } from '@angular/material/select';
@Component({
  selector: 'app-registration',
  standalone: true,
  imports: [RouterModule, FormsModule, ReactiveFormsModule, CommonModule, NgSelectModule, NavigationComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {


  loading = false;
  submitted = false;
  returnUrl: string | undefined;
  regFailed = false;
  error = '';
  invite: any;
  africanCountryCodes: Country[] = AFRICAN_COUNTRY_CODES;
  selectedCountryCode: string = '+27';
  registrationForm;



  constructor(private router: Router,
    public authenticationService: GlobalProvider,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute) {

    this.registrationForm = this.formBuilder.group({
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      username: ['', Validators.required],
      idNumber: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', Validators.required],
      countryCode: ['+27', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],
      passwordConfirm: ['', Validators.required],
      role: ['ROLE_POLICY_HOLDER', Validators.required],

    },{
      validator: this.passwordMatchValidator  // Apply the custom password match validator
    });
  }

  passwordMatchValidator(formGroup: FormGroup): null {
    const password = formGroup.get('password')?.value;
    const confirmPassword = formGroup.get('passwordConfirm')?.value;
    if (password !== confirmPassword) {
      formGroup.get('passwordConfirm')?.setErrors({ passwordMismatch: true });
    } else {
      formGroup.get('passwordConfirm')?.setErrors(null);
    }
    return null;
  }

  ngOnInit() {
    let inviteUuid = this.route.snapshot.queryParams['invite'];
    if (inviteUuid) {
      this.authenticationService.fetchInvite(inviteUuid).subscribe((response: any) => {
        this.invite = response;

        this.registrationForm = this.formBuilder.group({
          firstname: [this.invite.firstname, Validators.required],
          lastname: [this.invite.lastname, Validators.required],
          username: [this.invite.username, Validators.required],
          countryCode: [this.invite.countryCode, Validators.required],
          idNumber: [this.invite.idNumber, Validators.required],
          email: [this.invite.email, [Validators.required, Validators.email]],
          phone: [this.invite.phone, Validators.required],
          password: ['', [Validators.required, Validators.minLength(8)]],
          passwordConfirm: ['', Validators.required],
          role: ['ROLE_POLICY_HOLDER', Validators.required],
        },{
          validator: this.passwordMatchValidator  // Apply the custom password match validator
        });
      });
    }
    else {
      this.registrationForm = this.formBuilder.group({
        firstname: ['', Validators.required],
        lastname: ['', Validators.required],
        idNumber: ['', Validators.required],
        username: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        phone: ['', Validators.required],
        countryCode: ['+27', Validators.required],
        password: ['', [Validators.required, Validators.minLength(8)]],
        passwordConfirm: ['', Validators.required],
        role: ['ROLE_POLICY_HOLDER', Validators.required],
      },{
        validator: this.passwordMatchValidator  // Apply the custom password match validator
      });
      console.log('no invite'); 
    }


    return this.route.snapshot.queryParams['returnUrl'] || '/';;
  }
  get f() { return this.registrationForm?.controls; }

  login() {
    this.router.navigate(['/login']);
  }

  onRegister() {

    if (this.registrationForm) {
      Object.keys(this.registrationForm.controls).forEach(field => {
        const control = this.registrationForm?.get(field);
        control?.markAsTouched();
      });
    }

    let data: any = this.registrationForm?.getRawValue();
   // data.username = data.email;
    //data.role = 'ROLE_POLICY_HOLDER';
    data.contactType = 'SMS';
    console.log('console ' + JSON.stringify(data));
    this.submitted = true;

    // stop here if form is invalid
    if (this.registrationForm?.invalid) {
      // If the form is invalid, prevent submission and show errors
      console.log('Form is invalid');
      return;
    }
    this.loading = true;

    if (this.invite) {
      data.authorities = [data.role];
      this.authenticationService.acceptInvite(this.invite.uuid, data).subscribe((response: any) => {
        console.log('Response is ' + JSON.stringify(response))
        this.regFailed = false;
        this.authenticationService.setRegistrationData(data);
        this.authenticationService.showSuccess('Registration successful, you may now login', 'Registration success')
        this.router.navigate(['/login']);

      });

    }
    else {
      data.username = data.email;
      this.authenticationService.register(data)
        .subscribe(
          (result: any) => {
            this.regFailed = false;
            this.authenticationService.setRegistrationData(data);
            this.router.navigate(['/register-confirm']);
          },
          (error: any) => {
            this.regFailed = true;
            console.log('Error is ' + JSON.stringify(error));
            this.error = error.error.message;
            this.authenticationService.showError(this.error, this.error);
            this.loading = false;

          });
    }
  }
}
