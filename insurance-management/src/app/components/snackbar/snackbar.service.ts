import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SnackbarComponent } from './snackbar.component';

@Injectable({
  providedIn: 'root'
})
export class SnackBarService {
  constructor(private snackBar: MatSnackBar) {}
  public openSnackBar(message: string, action: string, snackType?: any) {
    const _snackType: any =
      snackType !== undefined ? snackType : 'done';

    this.snackBar.openFromComponent(SnackbarComponent, {
      duration: 2000,
      horizontalPosition: 'left',
      verticalPosition: 'bottom',
      data: { message: message, snackType: _snackType }
    });
  }
}