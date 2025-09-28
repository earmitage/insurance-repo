import { Product } from "./product";

export interface Subscription{
    uuid: string;
    subscriptionDate: string;
    subscriptionExpiryDate: string;
    amountPaid:number;
    active: boolean;
    username: string;
    product: Product;
    paymentUuids?: string[];
  }