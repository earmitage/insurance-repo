package co.za.insurance.policy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.earmitage.core.security.repository.Subscription;
import com.earmitage.core.security.repository.SubscriptionRepository;
import com.earmitage.core.security.repository.UserRepository;

@RestController
public class PolicyController {

    private final PolicyRepository policyRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    private final UserRepository userRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    public PolicyController(PolicyRepository policyRepository, BeneficiaryRepository beneficiaryRepository,
            final UserRepository userRepository,  
            final SubscriptionRepository subscriptionRepository) {
        this.policyRepository = policyRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @PostMapping(value = "${app.url}/policy-holders/{username}/policies/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Policy> createPolicy(@PathVariable final String username,
            @Valid @RequestBody AddPolicy policyDetails) {
        List<Subscription> list = subscriptionRepository.findByUserUsernameAndSubscriptionExpiryDateAfter(username, LocalDateTime.now());
        if(list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No active subcription found");
        }
        Policy policy = new Policy();
        policy.setCreatedAt(LocalDateTime.now());
        policy.setPolicyNumber(policyDetails.getPolicyNumber());
        policy.setInsuranceCompany(policyDetails.getInsuranceCompany());
        policy.setPolicyType(PolicyType.valueOf(policyDetails.getPolicyType()));
        policy.setCoverageAmount(policyDetails.getCoverageAmount());
        policy.setMonthlyPremium(policyDetails.getMonthlyPremium());
        policy.setStatus(policyDetails.getStatus());
        policy.setAddressLine1(policyDetails.getAddressLine1());
        policy.setCity(policyDetails.getCity());
        policy.setCountry(policyDetails.getCountry());
        policy.setPostalCode(policyDetails.getPostalCode());
        policy.setRegion(policyDetails.getRegion());
        policy.setDeceased(policyDetails.getDeceased());
        policy.setOwner(userRepository.findByUsernameAndActiveTrue(username));
        Policy savedPolicy = policyRepository.save(policy);
        policyDetails.getBeneficiaries().forEach(ben -> {
            Beneficiary beneficiary = new Beneficiary();
            beneficiary.setDateOfBirth(ben.getDateOfBirth());
            beneficiary.setFullName(ben.getFullName());
            beneficiary.setIdNumber(ben.getIdNumber());
            beneficiary.setPolicy(savedPolicy);
            beneficiary.setRelationship(ben.getRelationship());
            beneficiary.setSharePercentage(ben.getSharePercentage());
            beneficiary.setIdType(ben.getIdType());
            beneficiary.setCountryCode(ben.getCountryCode());
            beneficiary.setPhone(ben.getPhone());
            beneficiary.setEmail(ben.getEmail());
            beneficiary.setLoginAllowed(ben.getLoginAllowed());
            beneficiary.setDeceased(ben.getDeceased());
            beneficiaryRepository.save(beneficiary);
        });
        return new ResponseEntity<>(savedPolicy, HttpStatus.CREATED);
    }

    @PutMapping(value = "${app.url}/policy-holders/{username}/policies/{uuid}/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AddPolicy> updatePolicy(@PathVariable final String username,
            @PathVariable("uuid") final String uuid, @Valid @RequestBody AddPolicy policyDetails) {

        Optional<Policy> byUuid = policyRepository.findByUuid(uuid);
        if (byUuid.isPresent()) {
            final Policy policy = byUuid.get();
            policy.setPolicyNumber(policyDetails.getPolicyNumber());
            policy.setInsuranceCompany(policyDetails.getInsuranceCompany());
            policy.setPolicyType(PolicyType.valueOf(policyDetails.getPolicyType()));
            policy.setCoverageAmount(policyDetails.getCoverageAmount());
            policy.setMonthlyPremium(policyDetails.getMonthlyPremium());
            policy.setStatus(policyDetails.getStatus());
            policy.setAddressLine1(policyDetails.getAddressLine1());
            policy.setCity(policyDetails.getCity());
            policy.setCountry(policyDetails.getCountry());
            policy.setPostalCode(policyDetails.getPostalCode());
            policy.setRegion(policyDetails.getRegion());
            Policy savedPolicy = policyRepository.save(policy);
            policyDetails.getBeneficiaries().forEach(ben -> {
                Beneficiary beneficiary = null;
                Optional<Beneficiary> optional = policy.getBeneficiaries().stream()
                        .filter(b ->b.getUuid().equals(ben.getUuid())).findFirst();
                if (optional.isPresent()) {
                    beneficiary = optional.get();
                } else {
                    beneficiary = new Beneficiary();
                }
                beneficiary.setDateOfBirth(ben.getDateOfBirth());
                beneficiary.setFullName(ben.getFullName());
                beneficiary.setIdNumber(ben.getIdNumber());
                beneficiary.setPolicy(savedPolicy);
                beneficiary.setRelationship(ben.getRelationship());
                beneficiary.setSharePercentage(ben.getSharePercentage());
                beneficiary.setIdType(ben.getIdType());
                beneficiary.setCountryCode(ben.getCountryCode());
                beneficiary.setPhone(ben.getPhone());
                beneficiary.setEmail(ben.getEmail());
                beneficiary.setLoginAllowed(ben.getLoginAllowed());
                beneficiary.setDeceased(ben.getDeceased());
                beneficiaryRepository.save(beneficiary);
            });
            return new ResponseEntity<>(new AddPolicy(savedPolicy), HttpStatus.OK);
        }
        // TODO
        throw new RuntimeException("No policy found");
    }

    @PreAuthorize("hasAuthority('ROLE_POLICY_HOLDER')")
    @GetMapping(value = "${app.url}/policy-holders/{username}/policies/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetPolicies> getAllPolicys(@PathVariable final String username) {
        List<AddPolicy> policyholders = policyRepository.findByOwnerUsername(username).stream().map(AddPolicy::new)
                .collect(Collectors.toList());
        List<Subscription> subscriptions = subscriptionRepository.findByUserUsernameAndSubscriptionExpiryDateAfter(username, LocalDateTime.now());
        return new ResponseEntity<>(new GetPolicies(policyholders,subscriptions), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("${app.url}/policy-holders/{username}/deceased/")
    public ResponseEntity<Void> updatePolicyHolderDeath(@PathVariable String username, @RequestBody DeceasedUpdate details) {
        policyRepository.findByOwnerUsername(username).forEach(policy ->{
            policy.setDeceased(true);
            policy.setDeceasedDate(details.getDeceasedDate());
            policyRepository.save(policy);
        });
        return new ResponseEntity<>(HttpStatus.OK);
    }
    

    @PutMapping("${app.url}/beneficiaries/deceased/{beneficiaryUuid}/")
    public ResponseEntity<Void> updateBeneficiaryDeath(@PathVariable String beneficiaryUuid, @RequestBody DeceasedUpdate details) {
        Beneficiary beneficiary = beneficiaryRepository.findByUuid(beneficiaryUuid)
                .orElseThrow(() ->new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiary not found"));
        beneficiary.setDeceased(true);
        beneficiary.setDeceasedDate(details.getDeceasedDate());

        beneficiaryRepository.save(beneficiary);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
