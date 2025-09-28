import { Subscription } from "./subscription";


export interface User {
  idNumber: string;
  tos: boolean;
  username: string;
  password: string;
  role: string;
  passwordConfirm: string;
  firstname: string;
  lastname: string;
  email: string;
  phone:string;
  fullName: string;
  token: string;
  address: Location;
  fcmToken:string;
  activated: boolean;
  verified: boolean;
  roles: string[];
  subscriptions: Subscription[];

}

