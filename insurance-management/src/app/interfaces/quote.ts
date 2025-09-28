export interface Quote {
  senderCountry: string;
  senderCurrency: string;
  sourceBank: string;
  recipientBankPapssId: string;
  recipientCountry: string;
  recipientCurrency: string;
  amount: number;
  invoice: boolean;

  narration?: string;
  reference?: string;
  paymentCategory?: string;
  sourceOfFunds?: string;
}
