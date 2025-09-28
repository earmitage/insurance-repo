
export interface TransactionQuote {

    usd_to_sender_currency_rate: number;
    usd_to_receiver_currency_rate: number;
    sender_to_receiver_currency_rate: number;

    sender_amount: number; //amount with no fees calculated amount if invoice
    receiver_amount: number;
    exchange_amount: number;
    sender_currency_fee_amount: number; //paps fee in local/sender currency
    usd_fee_amount: number; //usd paps fee
    time: string;
}