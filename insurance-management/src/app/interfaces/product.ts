export interface Product {
    uuid: string;
    name: string;
    description: string;
    monthlyCost: string;
    annualCost: string;
    frequency: SubscriptionFrequency;
  }
  
  export type SubscriptionFrequency = 'MONTHLY' | 'ANNUAL'; 