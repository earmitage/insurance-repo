import { Component, AfterViewInit } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { NgIf, NgFor, CommonModule } from '@angular/common';
import Swiper from 'swiper';
import 'swiper/css/bundle';
import { NavigationComponent } from '../../navigation/navigation.component';

@Component({
  selector: 'app-about-us',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatDividerModule,
    CommonModule,
    NavigationComponent
],
  templateUrl: './about-us.component.html',
  styleUrls: ['./about-us.component.scss']
})
export class AboutUsComponent implements AfterViewInit {
  images = [
    {
      src: 'https://images.unsplash.com/photo-1588776814546-ec7b03dd06ed?auto=format&fit=crop&w=900&q=80',
      alt: 'Helping Families'
    },
    {
      src: 'https://images.unsplash.com/photo-1588776814184-0e498b2a8f2a?auto=format&fit=crop&w=900&q=80',
      alt: 'Insurance Protection'
    },
    {
      src: 'https://images.unsplash.com/photo-1605902711622-cfb43c4437d2?auto=format&fit=crop&w=900&q=80',
      alt: 'Data Integrity'
    }
  ];

  ngAfterViewInit(): void {
    new Swiper('.swiper', {
      spaceBetween: 30,
      slidesPerView: 1,
      loop: true,
      pagination: {
        el: '.swiper-pagination',
        clickable: true
      },
      navigation: {
        nextEl: '.swiper-button-next',
        prevEl: '.swiper-button-prev'
      }
    });
  }
}
