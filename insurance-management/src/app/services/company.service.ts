import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { GlobalProvider } from './globals';
import { Company, CreateCompanyDto, UpdateCompanyDto } from '../interfaces/company';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {

  private options = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    })
  };

  constructor(private http: HttpClient, private global: GlobalProvider) {}

  // Get all companies
  getCompanies(): Observable<Company[]> {
    return this.http.get<Company[]>(`${this.global.baseUrl}/admin/companies`, this.options)
      .pipe(map(response => response));
  }

  // Get company by ID
  getCompany(id: string): Observable<Company> {
    return this.http.get<Company>(`${this.global.baseUrl}/admin/companies/${id}`, this.options)
      .pipe(map(response => response));
  }

  // Create new company
  createCompany(company: CreateCompanyDto): Observable<Company> {
    return this.http.post<Company>(`${this.global.baseUrl}/admin/companies`, company, this.options)
      .pipe(map(response => response));
  }

  // Update existing company
  updateCompany(id: string, company: UpdateCompanyDto): Observable<Company> {
    return this.http.put<Company>(`${this.global.baseUrl}/admin/companies/${id}`, company, this.options)
      .pipe(map(response => response));
  }

  // Delete company
  deleteCompany(id: string): Observable<void> {
    return this.http.delete<void>(`${this.global.baseUrl}/admin/companies/${id}`, this.options)
      .pipe(map(response => response));
  }
}