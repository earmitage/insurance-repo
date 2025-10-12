package co.za.insurance.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PolicyholderPolicyBeneficiaryUpload {

    // Policyholder fields
    private String policyholderId;
    private String policyholderName;
    private String policyholderSurname;
    private String policyholderPhysicalAddress;
    private String policyholderEmail;
    private String policyholderContactNumber;

    // Policy fields
    private String insuranceProvider;
    private String policyNumber;
    private String policyType;
    private Double policyCoverageAmount;

    // Beneficiary fields
    private String beneficiaryId;
    private String beneficiaryName;
    private String beneficiarySurname;
    private String beneficiaryRelationship;
    private String beneficiaryContactNumber;
    private Double beneficiaryCoveragePercent;
}
