
import { User } from "./user";

export interface Event {

    animalCategory: string;
    uuid: string;
    date: number;
    estimatedDate: boolean;
    eventType: string;
    description: string;
    notes: string;
    farmUuid: string;
    plantationUuid: string;
    farmAnimalUuid: string;
    amount: number;
    quantity: number;
    actionedBy: User;
    animalType: string;
    // project: Project;
    paymentMethod: string;
    breed:string;
    feedType:string;
}
