import { Location } from "./location";
export interface Contact {

    farmUuid: string;
    uuid: string;
    name: string;
    phoneNumber: string;
    email: string;
    website: string;
    notes: string;
    location: Location;
}