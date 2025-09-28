package co.za.insurance.policy;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    
    List<Beneficiary> findByPolicyUuid(String policyUuid);
    
    Optional<Beneficiary> findByUuid(String uuid);
}
