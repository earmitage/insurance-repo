import { Location } from './location';

export interface Participant {

    bic: string;
    name: string;
    countryCode: string;
    status: string;
    paymentschemas: PaymentschemasType[];
    currencies: CurrenciesType;
    online: boolean;
    inst_id: string;

}

export interface PaymentschemasType {
    paymentschemas: string[];
}

export interface CurrenciesType {
    currencies: string[];
}

export interface PapssParticipants {
    participants: Participant[]
}


