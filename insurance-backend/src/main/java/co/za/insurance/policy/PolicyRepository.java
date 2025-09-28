package co.za.insurance.policy;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    public List<Policy> findByOwnerUsername(final String username);

    public Optional<Policy> findByUuid(final String uuid);

    public boolean existsByPolicyNumber(final String policyNumber);
}
