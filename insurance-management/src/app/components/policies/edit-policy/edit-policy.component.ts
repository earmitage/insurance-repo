import { AfterViewInit, OnInit, ViewChild } from '@angular/core';
import { Component } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { first } from 'rxjs/operators';
import { PolicyholderService } from '../../../services/policyholder.service';
import { GlobalProvider } from '../../../services/globals';
import { CommonModule } from '@angular/common';
import { NavigationComponent } from '../../../navigation/navigation.component';
import { NgSelectModule } from '@ng-select/ng-select';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { MatIconModule } from '@angular/material/icon';
import { AFRICAN_COUNTRY_CODES, Country } from '../../../interfaces/country';
import { PolicyService } from '../policies/policy.service';
import { Policy } from '../../../interfaces/policy';

declare var google: any;
/// <reference types="@types/googlemaps" />
declare global {
  interface Window {
    initAutocomplete: () => void;
  }
}
@Component({
  templateUrl: './edit-policy.component.html',
  imports: [FormsModule, ReactiveFormsModule, CommonModule, NavigationComponent, NgSelectModule, MatSelectModule,

    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule,
    FontAwesomeModule,
    MatIconModule
  ],
  styleUrls: ['./edit-policy.component.scss']
})
export class EditPolicyComponent implements OnInit, AfterViewInit {


  policyType: string = '';

  policyForm!: FormGroup;
  isCollapsed: boolean = false;
  iconCollapse: string = 'icon-arrow-up';
  @ViewChild('addresstext') addresstext: any;

  africanCountryCodes: Country[] = AFRICAN_COUNTRY_CODES; // Bind the African country list to a component variable
  selectedCountryCode: string = '+27'; // D

  constructor(
    private formBuilder: FormBuilder,
    public policyHolderServices: PolicyholderService,
    public global: GlobalProvider,
    private route: ActivatedRoute,
    private router: Router,
    private policyService: PolicyService) { }

  ngOnInit(): void {
    this.initializeForm(this.policyService.getSelectedPolicy());
    this.loadGoogleMapsScript();
  }

  initializeForm(policy: Policy) {

    console.log(JSON.stringify(policy));
    this.policyForm = this.formBuilder.group({
      policyNumber: [policy.policyNumber, Validators.required],
      insuranceCompany: [policy.insuranceCompany, Validators.required],
      policyType: [policy.policyType, Validators.required],
      coverageAmount: policy.coverageAmount,
      monthlyPremium: policy.monthlyPremium,
      status: policy.status,
      addressLine1: policy.addressLine1,
      city: policy.city,
      region: policy.region,
      postalCode: policy.postalCode,
      country: policy.country,
      beneficiaries: this.formBuilder.array([])
    });

    const beneficiariesFormArray = this.policyForm.get('beneficiaries') as FormArray;
    policy.beneficiaries.forEach(beneficiary => {
      beneficiariesFormArray.push(this.formBuilder.group({
        uuid: [beneficiary.uuid],
        fullName: [beneficiary.fullName, Validators.required],
        relationship: [beneficiary.relationship, Validators.required],
        sharePercentage: [beneficiary.sharePercentage, Validators.required],
        idNumber: [beneficiary.idNumber, Validators.required],
        idType: [beneficiary.idType, Validators.required],
        countryCode: [beneficiary.countryCode, Validators.required],
        phone: [beneficiary.phone],
        email: [beneficiary.email, [Validators.email]],
        loginAllowed: [beneficiary.loginAllowed]
      }));
    });

  }
  ngAfterViewInit() {
    window.initAutocomplete = this.initAutocomplete.bind(this);
  }
  cancel() {
    this.router.navigate(['/policies']);
  }
  loadGoogleMapsScript() {
    const script = document.createElement('script');
    script.src = `https://maps.googleapis.com/maps/api/js?key=AIzaSyC19lTJvUeVkPfWvKsx1HP5BT9En6QtGeM&libraries=places&callback=initAutocomplete`;
    script.async = true;
    script.defer = true;
    document.head.appendChild(script);
  }
  validateEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  updatePolicy() {

    Object.keys(this.policyForm.controls).forEach(field => {
      const control = this.policyForm.get(field);
      control?.markAsTouched();
    });
    if (!this.validatePhoneOrEmail()) {
      return
    }
    if (this.policyForm?.invalid) {
      const email: string = this.policyForm.get('beneficiaries')?.value.map((beneficiary: any) => beneficiary.email);
      console.log('email >' + email + this.validateEmail(email) + '<' + email.length);
      if (email && email.length > 1 && !this.validateEmail(email)) {
        this.beneficiariesMessage = 'Beneficiary email is not valid';
        this.global.showError('Beneficiary email is not valid', 'Close');
        return;
      }
      return;


    }


    this.policyHolderServices.updatePolicy(this.policyForm.value, this.policyService.getSelectedPolicy().uuid)
      .pipe(first())
      .subscribe(
        {
          next: (data: any) => {
            this.global.showSuccess('Policy successfully updated', 'Close');
            this.router.navigate(['/policies']);
          },
          error: (error) => {
            console.error('Error updating policy:', JSON.stringify(error));
            this.global.showError('Failed to updated the policy', 'Close');

          }
        });
  }

  validPolicyDetails(): boolean {
    let policyNumber = this.policyForm.get('policyNumber')?.value;
    let insuranceCompany = this.policyForm.get('insuranceCompany')?.value;
    let policyType = this.policyForm.get('policyType')?.value;
    if (policyNumber == '' || insuranceCompany == '' || policyType == '') {
      this.beneficiariesMessage = 'Enter Policy details first';
      return false;
    }
    return true;
  }

  get beneficiaries() {
    return (this.policyForm.get('beneficiaries') as FormArray);
  }

  beneficiariesMessage = '';
  idNumberPlaceHolder = 'Enter your SA ID';
  idNumberHeading = 'ID Number';


  addBeneficiary() {
    if (!this.validPolicyDetails()) {
      return;

    }


    this.beneficiariesMessage = '';
    if (this.beneficiaries.length > 0) {
      let totalShare: number = 0;
      this.beneficiaries.controls.forEach((control: any) => {
        totalShare += control.get('sharePercentage').value;
      });
      if (totalShare >= 100) {
        this.global.showError('Total share percentage ' + totalShare + ' cannot exceed 100%', 'Total share percentage cannot exceed 100%');
        this.beneficiariesMessage = 'Total share percentage  ' + totalShare + ' cannot exceed 100%.';
        return;
      }
      let latestIdNumber = this.beneficiaries.at(this.beneficiaries.length - 1)?.get('idNumber')?.value || '';
      if (latestIdNumber == null || latestIdNumber == '') {
        this.global.showError('ID number cannot be empty', 'ID number cannot be empty');
        this.beneficiariesMessage = 'ID number cannot be empty';
        return;
      }
      let latestIdType = this.beneficiaries.at(this.beneficiaries.length - 1)?.get('idType')?.value || '';
      if (latestIdType == 'SAID' && latestIdNumber.length != 13) {
        this.global.showError('ID number must be 13 digits for South African ID', 'ID number must be 13 digits for South African ID');
        this.beneficiariesMessage = 'ID number must be 13 digits for South African ID';
        return;
      }


      // Check if the ID number already exists in the form array
      const idNumber = this.policyForm.get('beneficiaries')?.value.map((beneficiary: any) => beneficiary.idNumber);
      if (idNumber.includes(this.policyForm.get('beneficiaries')?.value.idNumber)) {
        this.global.showError('ID number already exists', 'ID number already exists');
        this.beneficiariesMessage = 'ID number already exists';
        return;
      }
      // Check if phone or email is provided
      if (!this.validatePhoneOrEmail()) {
        return
      }
    }

    this.beneficiariesMessage = '';

    this.beneficiaries.push(this.formBuilder.group({
      fullName: ['', Validators.required],
      relationship: ['', Validators.required],
      sharePercentage: ['', [Validators.required, Validators.min(1), Validators.max(100)]],
      idNumber: ['', Validators.required],
      idType: ['SAID', Validators.required],
      phone: [''],
      email: ['', [Validators.email]],
      countryCode: ['+27', Validators.required],
      loginAllowed: [false, Validators.required]
    }));
  }
  validatePhoneOrEmail(): boolean {

    const phone = this.beneficiaries.at(this.beneficiaries.length - 1)?.get('phone')?.value || '';
    const email = this.beneficiaries.at(this.beneficiaries.length - 1)?.get('email')?.value || '';
    if (phone == '' && email == '') {
      this.global.showError('Phone or email must be provided', 'X');
      this.beneficiariesMessage = 'Phone or email must be provided';
      return false;
    }
    return true
  }

  onIdTypeSelectionChange(event: any) {
    const selectedValue = event.value;

    // Change placeholder text based on selection
    if (selectedValue === 'SAID') {
      this.idNumberPlaceHolder = 'Enter your SA ID';
      this.idNumberHeading = 'SA ID';
    } else if (selectedValue === 'Passport') {
      this.idNumberHeading = 'Passport Number';
      this.idNumberPlaceHolder = 'Enter your Passport Number';
    }
  }
  removeBeneficiary(index: number) {
    this.beneficiaries.removeAt(index);
  }


  collapsed(event: any): void {
    // console.log(event);
  }

  expanded(event: any): void {
    // console.log(event);
  }

  toggleCollapse(): void {
    this.isCollapsed = !this.isCollapsed;
    this.iconCollapse = this.isCollapsed ? 'icon-arrow-down' : 'icon-arrow-up';
  }

  initAutocomplete(): void {
    // This is the HTML input element that we want to attach the autocomplete
    //const input = document.getElementById('addressLine1') as HTMLInputElement;

    const autocomplete = new google.maps.places.Autocomplete(
      this.addresstext.nativeElement,
      {
        componentRestrictions: { country: 'ZA' },
        //types: [this.addressType]  // 'establishment' / 'address' / 'geocode' // we are checking all types
      }
    );

    //const autocomplete = new google.maps.places.Autocomplete(input, {
    //types: ['address'],
    //componentRestrictions: { country: 'za' } // You can restrict to a country if needed
    //});

    autocomplete.addListener('place_changed', () => {
      const place = autocomplete.getPlace();
      if (!place.geometry) {
        console.log("No details available for input: '" + place.name + "'");
        return;
      }

      // Capture the address components
      this.fillAddressFields(place);
    });
  }

  fillAddressFields(place: any): void {
    let addressLine1 = '';
    let addressLine2 = '';
    let city = '';
    let postalCode = '';

    for (const component of place.address_components) {
      const types = component.types;
      if (types.includes('route')) {
        addressLine1 = component.long_name;
      } else if (types.includes('street_number')) {
        addressLine2 = component.long_name;
      } else if (types.includes('locality')) {
        city = component.long_name;
      } else if (types.includes('postal_code')) {
        postalCode = component.long_name;
      }
    }
    addressLine1 = addressLine2 + ' ' + addressLine1

    // Update the form controls with the extracted values
    this.policyForm.patchValue({
      addressLine1,
      city,
      postalCode
    });
  }


}