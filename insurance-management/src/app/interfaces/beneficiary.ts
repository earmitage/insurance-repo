import { Policy } from './policy';

export interface Beneficiary {
  uuid: string;
  policy: Policy;
  fullName: string;
  idNumber: string;
  relationship: string;
  dateOfBirth: string;
  sharePercentage: number;
  idType: string;
  phone: string;
  email: string;
  countryCode: string;
  loginAllowed: boolean;
}
