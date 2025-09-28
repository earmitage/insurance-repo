package co.za.insurance.policy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddPolicy {

    private String policyNumber;
    private String insuranceCompany;
    private String policyType;
    private Double coverageAmount;
    private Double monthlyPremium;
    private String status;
    private String addressLine1;
    private String city;
    private String region;
    private String postalCode;
    private String country;
    private String uuid;
    private Boolean deceased;
    private LocalDate deceasedDate;
    

    private List<AddBeneficiary> beneficiaries = new ArrayList<>();

    public AddPolicy(final Policy policy) {
        this.policyNumber = policy.getPolicyNumber();
        this.insuranceCompany = policy.getInsuranceCompany();
        this.policyType = policy.getPolicyType().name();
        this.coverageAmount = policy.getCoverageAmount();
        this.monthlyPremium = policy.getMonthlyPremium();
        this.status = policy.getStatus();
        this.addressLine1 = policy.getAddressLine1();
        this.city = policy.getCity();
        this.country = policy.getCountry();
        this.postalCode = policy.getPostalCode();
        this.region = policy.getRegion();
        this.uuid = policy.getUuid();
        this.deceased = policy.getDeceased();
        this.deceasedDate = policy.getDeceasedDate();
        policy.getBeneficiaries().forEach(b -> {
            beneficiaries.add(new AddBeneficiary(b.getUuid(), b.getFullName(), b.getIdNumber(), b.getRelationship(),
                    b.getDateOfBirth(), b.getSharePercentage(), b.getIdType(),b.getCountryCode(), b.getPhone(), b.getEmail(), b.getLoginAllowed(), b.getDeceased(), b.getDeceasedDate()));

        });

    }
}