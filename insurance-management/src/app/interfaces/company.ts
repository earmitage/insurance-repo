export interface Company {
  id: string;
  name: string;
  address: string;
  city: string;
  region: string;
  postalCode: string;
  country: string;
  phoneNumber?: string;
  email?: string;
  website?: string;
  registrationNumber?: string;
  createdAt: string;
  updatedAt: string;
}

// DTO for creating a new company
export interface CreateCompanyDto {
  name: string;
  address: string;
  city: string;
  region: string;
  postalCode: string;
  country: string;
  phoneNumber?: string;
  email?: string;
  website?: string;
  registrationNumber?: string;
}

// DTO for updating an existing company
export interface UpdateCompanyDto {
  name?: string;
  address?: string;
  city?: string;
  region?: string;
  postalCode?: string;
  country?: string;
  phoneNumber?: string;
  email?: string;
  website?: string;
  registrationNumber?: string;
}