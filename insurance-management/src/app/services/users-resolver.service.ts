import { Injectable } from '@angular/core';
import { Resolve } from '@angular/router';
import { Observable } from 'rxjs';
import {tap} from "rxjs/operators";
import {GlobalProvider} from "./globals";
import { Policy } from '../interfaces/policy';
import { PolicyholderService } from './policyholder.service';


@Injectable()
export class UserDataResolver implements Resolve<Observable<Policy[]>> {

  constructor(private policyholderService: PolicyholderService, private global: GlobalProvider) {}

  resolve() {
    return this.policyholderService.fetchUserPolicies().pipe(tap(this.processPolices));
  }

  private processPolices(policies:Policy[]): void {
    console.debug("Processing User's policies", policies);
    if(this.global){
      this.global.policies = policies;
      this.global.policiesSubject.next(policies)
      if (policies.length > 0) {
        this.global.currentPolicySubject.next(policies[0]);
      }
    }
  }
}
