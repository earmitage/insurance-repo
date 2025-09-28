import { Routes } from "@angular/router";
import { RegisterComponent } from "./components/register/register.component";
import { LoginComponent } from "./components/login/login.component";
import { AddPolicyComponent } from "./components/policies/add-policy/add-policy.component";
import { AllPoliciesComponent } from "./components/policies/policies/policies.component";
import { RegisterConfirmComponent } from "./components/register-confirm/register-confirm.component";
import { AdminComponent } from "./components/admin/admin.component";
import { EditPolicyComponent } from "./components/policies/edit-policy/edit-policy.component";
import { ForgotPasswordPinComponent } from "./components/forgot-password-pin/forgot-password-pin.component";
import { ForgotPasswordComponent } from "./components/forgot-password/forgot-password.component";
import { ProfileComponent } from "./components/profile/profile.component";
import { AboutUsComponent } from "./components/about-us/about-us.component";
import { SubscribeComponent } from "./components/subscribe/subscribe.component";
import { FileUploadComponent } from "./components/admin/file-upload/file-upload.component";
import { CompanyManagementComponent } from "./components/admin/company-management/company-management.component";
import { AdminPoliciesComponent } from "./components/admin/admin-policies/admin-policies.component";
import { AuthGuard } from "./services/auth-guard";
//import { AdminGuard } from "./services/admin-guard";

export const routes: Routes = [
    { path: 'register', component: RegisterComponent },
    { path: 'register-confirm', component: RegisterConfirmComponent },
    { path: 'login', component: LoginComponent },
    { path: 'add-policy', component: AddPolicyComponent },
    { path: 'edit-policy', component: EditPolicyComponent },
    { path: 'policies', component: AllPoliciesComponent },
    { path: 'forgot-password', component: ForgotPasswordComponent },
    { path: 'forgot-password-pin', component: ForgotPasswordPinComponent },
    { path: 'my-profile', component: ProfileComponent },
    { path: 'about-us', component: AboutUsComponent },
    { path: 'subscribe', component: SubscribeComponent },
   
    { path: 'admin', component: AdminComponent },
    { path: 'admin/policies', component: AdminPoliciesComponent, canActivate: [AuthGuard] },
    { path: 'admin/companies', component: CompanyManagementComponent, canActivate: [AuthGuard] },
    { path: 'admin/file-upload', component: FileUploadComponent, canActivate: [AuthGuard] },
    { path: '', redirectTo: '/policies', pathMatch: 'full' },

  ];