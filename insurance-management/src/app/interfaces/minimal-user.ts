import { Policy } from "./policy";
import { Subscription } from "./subscription";
import { User } from "./user";

export interface MinimalUser extends User {

  policies: Policy[];

}