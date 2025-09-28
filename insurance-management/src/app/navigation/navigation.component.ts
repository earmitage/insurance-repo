import { AfterViewInit, Component, OnInit } from '@angular/core';
import { GlobalProvider } from '../services/globals';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navigation',
  imports: [CommonModule],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss'
})
export class NavigationComponent implements OnInit {

  logoutLabel: string = '';
  role: string
  currentUser: any;
  constructor(private router: Router,
    public global: GlobalProvider
  ) {

    if (this.global.loggedIn) {
      this.logoutLabel = 'Logout';
    }
  }
  ngOnInit(): void {
    if (this.global.loggedIn) {
      this.logoutLabel = 'Logout';
    }
    try {
      if (this.global.currentUserValue && this.global.currentUserValue.roles && this.global.currentUserValue.roles.length > 0) {
        this.currentUser = this.global.currentUserValue.username;
        this.role = this.global.currentUserValue.roles[0];
        this.logoutLabel = 'Logout';
      }
    } catch (error) {
     // this.router.navigate(['/login']);
     this.global.loggedIn = false;

    }
  }

  myProfile(){
    this.router.navigate(['/my-profile']);
  }
  

  logout() {
    this.global.logout();
    this.router.navigate(['/login']);
  }
}
