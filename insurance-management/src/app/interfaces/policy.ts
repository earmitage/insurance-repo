import { User } from './user';
import { PolicyType } from './policy-type';
import { Beneficiary } from './beneficiary';
import { Company } from './company';
export interface Policy {
  uuid: string; // This will be a UUID string
  owner: User; // Reference to the User (owner of the policy)
  company?: Company; // Reference to the Company that owns this policy
  policyNumber: string;
  insuranceCompany: string;
  lastUpdated: string; // ISO 8601 string or Date string
  createdAt: string; // ISO 8601 string or Date string
  policyType: PolicyType; // Enum or string representing policy type


  coverageAmount: number;
  monthlyPremium: number;
  status: string;
  beneficiaries: Beneficiary[]; // List of Beneficiaries

  addressLine1: string;
  city: string;
  region: string;
  postalCode: string;
  country: string;
}
/*

export interface AddPolicy {
  policyNumber: string;
  insuranceCompany: string;
  policyType: string;

  beneficiaries: Beneficiary[];
}
  */
