import { Policy } from "./policy";
import { Subscription } from "./subscription";

export interface GetPolicies {
    policies: Policy[];
    activeSubcriptions: Subscription[];

}
