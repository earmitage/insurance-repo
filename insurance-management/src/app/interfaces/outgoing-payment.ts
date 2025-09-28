
export interface OutgoingPayment {
    uuid: string;
    capturedDate;
    submittedDate;
    senderFirstName: string;
    senderLastName: string;
    senderAccountNumber: string;
    senderCurrency: string;
    senderWalletNumber: string;
    senderBank: string;
    senderBankBic: string;
    senderEmail: string;
    senderPhoneNumber: string;
    senderAmount: number;
    senderAddress: string;
    senderCity: string;
    senderProvince: string;
    senderIdentity: string;
    senderCountry: string;
    recipientFirstName: string;
    recipientLastName: string;
    recipientAccountNumber: string;
    recipientCurrency: string;
    recipientWalletNumber: string;
    recipientBank: string;
    recipientBankBic: string;
    recipientEmail: string;
    recipientPhoneNumber: string;
    recipientAmount: number;
    recipientAddress: string;
    recipientCity: string;
    recipientProvince: string;
    recipientIdentity: string;
    recipientCountry: string;
    capturedBy: string;
    authorizedBy: string;
    status: string;
    message: string;
    narration: string;
    reference: string;
    paymentCategory:string;
    sourceOfFunds: string;
}

export interface OutgoingPaymentView extends OutgoingPayment {
    senderMoney?: string;
    recipientMoney?: string;
}
