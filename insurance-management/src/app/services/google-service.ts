import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class GoogleApiService {
  private scriptLoaded = false;

  constructor() { }

  load(): Promise<void> {
    if (this.scriptLoaded) {
      return Promise.resolve();
    }

    return new Promise((resolve, reject) => {
      const script = document.createElement('script');

      script.src = `https://maps.googleapis.com/maps/api/js?libraries=places&key=AIzaSyC19lTJvUeVkPfWvKsx1HP5BT9En6QtGeM&callback=Function.prototype`;
      script.async = true;
      script.defer = true;

      script.onload = () => {
        this.scriptLoaded = true;
        resolve();
      };

      script.onerror = (error) => reject(error);

      document.head.appendChild(script);
    });
  }
}
