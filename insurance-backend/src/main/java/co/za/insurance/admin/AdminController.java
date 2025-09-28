package co.za.insurance.admin;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.earmitage.core.security.repository.Subscription;
import com.earmitage.core.security.repository.SubscriptionRepository;
import com.earmitage.core.security.repository.User;
import com.earmitage.core.security.repository.UserRepository;

import co.za.insurance.Role;
import co.za.insurance.company.Company;
import co.za.insurance.company.CompanyRepository;
import co.za.insurance.policy.AddBeneficiary;
import co.za.insurance.policy.AddPolicy;
import co.za.insurance.policy.Beneficiary;
import co.za.insurance.policy.BeneficiaryRepository;
import co.za.insurance.policy.GetPolicies;
import co.za.insurance.policy.Policy;
import co.za.insurance.policy.PolicyRepository;
import co.za.insurance.policy.PolicyType;

@RestController
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CompanyRepository companyRepository;
    
    @GetMapping(value = "${app.url}/admin/users/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<MinimalUser> fetchUsers() {
        return userRepository.findUsersByRole(Role.ROLE_POLICY_HOLDER.name()).stream().map(u -> {
            List<Subscription> subscriptions = subscriptionRepository.findByUserUsername(u.getUsername());
            List<Policy> policies = policyRepository.findByOwnerUsername(u.getUsername());
            MinimalUser minimalUser = new MinimalUser(u, policies, subscriptions);
            return minimalUser;
        }).toList();
    }

    @PostMapping(value = "${app.url}/admin/upload/policies", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UploadResult> uploadPolicies(@Valid @RequestBody PolicyUploadRequest request) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        List<AddPolicy> policies = request.getData();
        Long companyId = request.getCompanyId();

        // Validate company exists if companyId is provided
        Company company = null;
        if (companyId != null) {
            company = companyRepository.findById(companyId).orElse(null);
            if (company == null) {
                errors.add("Company with ID " + companyId + " not found");
                UploadResult result = new UploadResult();
                result.setSuccessCount(0);
                result.setErrorCount(policies.size());
                result.setErrors(errors);
                result.setMessage("Company validation failed");
                return ResponseEntity.badRequest().body(result);
            }
        }

        for (int i = 0; i < policies.size(); i++) {
            try {
                AddPolicy policyDetails = policies.get(i);

                // Validate required fields
                if (policyDetails.getPolicyNumber() == null || policyDetails.getPolicyNumber().trim().isEmpty()) {
                    errors.add("Row " + (i + 1) + ": Policy number is required");
                    errorCount++;
                    continue;
                }

                if (policyDetails.getInsuranceCompany() == null || policyDetails.getInsuranceCompany().trim().isEmpty()) {
                    errors.add("Row " + (i + 1) + ": Insurance company is required");
                    errorCount++;
                    continue;
                }

                if (policyDetails.getPolicyType() == null || policyDetails.getPolicyType().trim().isEmpty()) {
                    errors.add("Row " + (i + 1) + ": Policy type is required");
                    errorCount++;
                    continue;
                }

                // Check if policy number already exists
                if (policyRepository.existsByPolicyNumber(policyDetails.getPolicyNumber())) {
                    errors.add("Row " + (i + 1) + ": Policy number " + policyDetails.getPolicyNumber() + " already exists");
                    errorCount++;
                    continue;
                }

                // Create new policy - for bulk upload, assign to admin user as placeholder
                User adminUser = userRepository.findUsersByRole(Role.ROLE_ADMIN.name()).stream().findFirst().orElse(null);
                if (adminUser == null) {
                    errors.add("Row " + (i + 1) + ": No admin user found to assign policy");
                    errorCount++;
                    continue;
                }

                Policy policy = new Policy();
                policy.setCreatedAt(LocalDateTime.now());
                policy.setPolicyNumber(policyDetails.getPolicyNumber());
                policy.setInsuranceCompany(policyDetails.getInsuranceCompany());
                policy.setPolicyType(PolicyType.valueOf(policyDetails.getPolicyType()));
                policy.setCoverageAmount(policyDetails.getCoverageAmount());
                policy.setMonthlyPremium(policyDetails.getMonthlyPremium());
                policy.setStatus(policyDetails.getStatus() != null ? policyDetails.getStatus() : "ACTIVE");
                policy.setAddressLine1(policyDetails.getAddressLine1());
                policy.setCity(policyDetails.getCity());
                policy.setCountry(policyDetails.getCountry());
                policy.setPostalCode(policyDetails.getPostalCode());
                policy.setRegion(policyDetails.getRegion());
                policy.setDeceased(policyDetails.getDeceased() != null ? policyDetails.getDeceased() : false);
                policy.setDeceasedDate(policyDetails.getDeceasedDate());
                policy.setOwner(adminUser);

                // Set company if provided
                if (company != null) {
                    policy.setCompany(company);
                }

                Policy savedPolicy = policyRepository.save(policy);

                // Add beneficiaries if present
                if (policyDetails.getBeneficiaries() != null) {
                    for (AddBeneficiary ben : policyDetails.getBeneficiaries()) {
                        Beneficiary beneficiary = new Beneficiary();
                        beneficiary.setDateOfBirth(ben.getDateOfBirth());
                        beneficiary.setFullName(ben.getFullName());
                        beneficiary.setIdNumber(ben.getIdNumber());
                        beneficiary.setPolicy(savedPolicy);
                        beneficiary.setRelationship(ben.getRelationship());
                        beneficiary.setSharePercentage(ben.getSharePercentage());
                        beneficiary.setIdType(ben.getIdType() != null ? ben.getIdType() : "SAID");
                        beneficiary.setCountryCode(ben.getCountryCode() != null ? ben.getCountryCode() : "+27");
                        beneficiary.setPhone(ben.getPhone());
                        beneficiary.setEmail(ben.getEmail());
                        beneficiary.setLoginAllowed(ben.getLoginAllowed() != null ? ben.getLoginAllowed() : false);
                        beneficiary.setDeceased(ben.getDeceased() != null ? ben.getDeceased() : false);
                        beneficiary.setDeceasedDate(ben.getDeceasedDate());
                        beneficiaryRepository.save(beneficiary);
                    }
                }

                successCount++;
            } catch (Exception e) {
                errors.add("Row " + (i + 1) + ": " + e.getMessage());
                errorCount++;
            }
        }

        UploadResult result = new UploadResult();
        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);
        result.setErrors(errors);
        result.setMessage(successCount + " policies uploaded successfully. " + errorCount + " errors occurred.");

        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "${app.url}/admin/upload/beneficiaries", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UploadResult> uploadBeneficiaries(@Valid @RequestBody List<AddBeneficiary> beneficiaries) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < beneficiaries.size(); i++) {
            try {
                AddBeneficiary beneficiaryDetails = beneficiaries.get(i);

                // Validate required fields
                if (beneficiaryDetails.getFullName() == null || beneficiaryDetails.getFullName().trim().isEmpty()) {
                    errors.add("Row " + (i + 1) + ": Full name is required");
                    errorCount++;
                    continue;
                }

                if (beneficiaryDetails.getIdNumber() == null || beneficiaryDetails.getIdNumber().trim().isEmpty()) {
                    errors.add("Row " + (i + 1) + ": ID number is required");
                    errorCount++;
                    continue;
                }

                // For standalone beneficiary upload, we need a default policy
                // In a real implementation, you might want to require a policy reference
                Policy defaultPolicy = policyRepository.findAll().stream().findFirst().orElse(null);
                if (defaultPolicy == null) {
                    errors.add("Row " + (i + 1) + ": No policy found to assign beneficiary");
                    errorCount++;
                    continue;
                }

                Beneficiary beneficiary = new Beneficiary();
                beneficiary.setFullName(beneficiaryDetails.getFullName());
                beneficiary.setIdNumber(beneficiaryDetails.getIdNumber());
                beneficiary.setRelationship(beneficiaryDetails.getRelationship());
                beneficiary.setDateOfBirth(beneficiaryDetails.getDateOfBirth());
                beneficiary.setSharePercentage(beneficiaryDetails.getSharePercentage());
                beneficiary.setIdType(beneficiaryDetails.getIdType() != null ? beneficiaryDetails.getIdType() : "SAID");
                beneficiary.setCountryCode(beneficiaryDetails.getCountryCode() != null ? beneficiaryDetails.getCountryCode() : "+27");
                beneficiary.setPhone(beneficiaryDetails.getPhone());
                beneficiary.setEmail(beneficiaryDetails.getEmail());
                beneficiary.setLoginAllowed(beneficiaryDetails.getLoginAllowed() != null ? beneficiaryDetails.getLoginAllowed() : false);
                beneficiary.setDeceased(beneficiaryDetails.getDeceased() != null ? beneficiaryDetails.getDeceased() : false);
                beneficiary.setDeceasedDate(beneficiaryDetails.getDeceasedDate());
                beneficiary.setPolicy(defaultPolicy);

                beneficiaryRepository.save(beneficiary);
                successCount++;
            } catch (Exception e) {
                errors.add("Row " + (i + 1) + ": " + e.getMessage());
                errorCount++;
            }
        }

        UploadResult result = new UploadResult();
        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);
        result.setErrors(errors);
        result.setMessage(successCount + " beneficiaries uploaded successfully. " + errorCount + " errors occurred.");

        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "${app.url}/admin/policies/", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<GetPolicies> getAllPolicies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String policyNumber,
            @RequestParam(required = false) String insuranceCompany,
            @RequestParam(required = false) String ownerUsername) {

        List<Policy> allPolicies = policyRepository.findAll();

        // Apply filters if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            allPolicies = allPolicies.stream()
                .filter(p -> (p.getPolicyNumber() != null && p.getPolicyNumber().toLowerCase().contains(searchLower)) ||
                           (p.getInsuranceCompany() != null && p.getInsuranceCompany().toLowerCase().contains(searchLower)) ||
                           (p.getOwner() != null && p.getOwner().getUsername() != null && p.getOwner().getUsername().toLowerCase().contains(searchLower)) ||
                           (p.getOwner() != null && p.getOwner().getFirstname() != null && p.getOwner().getFirstname().toLowerCase().contains(searchLower)) ||
                           (p.getOwner() != null && p.getOwner().getLastname() != null && p.getOwner().getLastname().toLowerCase().contains(searchLower)))
                .toList();
        }

        if (policyNumber != null && !policyNumber.trim().isEmpty()) {
            allPolicies = allPolicies.stream()
                .filter(p -> p.getPolicyNumber() != null && p.getPolicyNumber().toLowerCase().contains(policyNumber.toLowerCase()))
                .toList();
        }

        if (insuranceCompany != null && !insuranceCompany.trim().isEmpty()) {
            allPolicies = allPolicies.stream()
                .filter(p -> p.getInsuranceCompany() != null && p.getInsuranceCompany().toLowerCase().contains(insuranceCompany.toLowerCase()))
                .toList();
        }

        if (ownerUsername != null && !ownerUsername.trim().isEmpty()) {
            allPolicies = allPolicies.stream()
                .filter(p -> p.getOwner() != null && p.getOwner().getUsername() != null && p.getOwner().getUsername().toLowerCase().contains(ownerUsername.toLowerCase()))
                .toList();
        }

        List<AddPolicy> policyDtos = allPolicies.stream()
            .map(AddPolicy::new)
            .toList();

        return new ResponseEntity<>(new GetPolicies(policyDtos, new ArrayList<>()), HttpStatus.OK);
    }

}

