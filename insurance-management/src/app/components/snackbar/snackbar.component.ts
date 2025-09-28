import { Component, OnInit, Inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MAT_SNACK_BAR_DATA } from '@angular/material/snack-bar';

   @Component({
     selector: 'snackbar',
     templateUrl: './snackbar.component.html',
     imports: [MatIconModule],
     styleUrls: ['./snackbar.component.scss']
   })
   export class SnackbarComponent implements OnInit {
     constructor(@Inject(MAT_SNACK_BAR_DATA) public data: any) {
       console.log(data); 
     }

     ngOnInit() {}

     get getIcon() {
       return this.data.snackType;
     }
   }