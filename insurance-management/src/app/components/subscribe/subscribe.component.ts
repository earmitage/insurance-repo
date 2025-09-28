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
import { Product } from '../../interfaces/product';
import { Subscription } from '../../interfaces/subscription';
import { User } from '../../interfaces/user';
@Component({
  selector: 'app-subscribe',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, CommonModule,
    NavigationComponent,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,

  ],
  templateUrl: './subscribe.component.html',
  styleUrls: ['./subscribe.component.scss']
})
export class SubscribeComponent {
  registrationConfirmForm!: FormGroup;
  product: Product | null = null;
   subscriptions: Subscription[] = [];
  loading = false;
  submitted = false;
  returnUrl: string | undefined;
  error = '';
  confFailed = false;

  constructor(private router: Router,
    private commonservice: GlobalProvider,
    private formBuilder: FormBuilder, private dialog: MatDialog,
    private route: ActivatedRoute) { }

  ngOnInit() {
    this.registrationConfirmForm = this.formBuilder.group({
      token: ['', Validators.required]

    });

    this.commonservice.fetchProducts().subscribe({
      next: (products: Product[]) => {
        this.product = products[0];
      },
      error: (err) => {
        console.error('Could not fetch products', err);
      }
    });

      this.commonservice.fetchCurrentUser().subscribe({
          next: (user: User) => {
            this.subscriptions = user.subscriptions;
            this.commonservice.setCurrentUser(user);
    
          },
          error: (err) => {
            console.error('Could not fetch subscriptions', err);
          }
        });

    // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }
  get f() { return this.registrationConfirmForm?.controls; }



  addPolicy() {
    this.router.navigate(['/add-policy']);
  }

  payNow() {

    let data = { amount: this.product?.annualCost, productUuid: this.product?.uuid, currency: 'ZAR' };

    this.commonservice.initiatePayment(data)
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
