import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { GlobalProvider } from './globals';
import { UploadResult } from './file-upload/file-upload.interfaces';
import { Company } from '../interfaces/company';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private options = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    })
  };

  constructor(private http: HttpClient, private global: GlobalProvider) {

  }

  fetchUsers(): Observable<any> {
    return this.http.get<any>(`${this.global.baseUrl}/admin/users/`, this.options)
      .pipe(map(response => {
        return response;
      }));
  }

  uploadData(endpoint: string, data: any[], companyId?: string): Observable<UploadResult> {
    const payload = companyId ? { data, companyId } : data;
    return this.http.post<UploadResult>(`${this.global.baseUrl}${endpoint}`, payload, this.options)
      .pipe(map(response => {
        return response;
      }));
  }

  // Company management methods
  getCompanies(): Observable<Company[]> {
    return this.http.get<Company[]>(`${this.global.baseUrl}/admin/companies`, this.options)
      .pipe(map(response => response));
  }

  // Policy search methods
  getAllPolicies(searchParams?: any): Observable<any> {
    let url = `${this.global.baseUrl}/admin/policies/`;

    if (searchParams) {
      const params = new URLSearchParams();
      Object.keys(searchParams).forEach(key => {
        if (searchParams[key] && searchParams[key].trim() !== '') {
          params.append(key, searchParams[key]);
        }
      });
      if (params.toString()) {
        url += '?' + params.toString();
      }
    }

    return this.http.get<any>(url, this.options)
      .pipe(map(response => response));
  }

}
