package com.earmitage.core.security.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserUsername(String username);
    List<Subscription> findByUserUsernameAndSubscriptionExpiryDateAfter(String username, LocalDateTime now);

    
    List<Subscription> findByProductUuid(String productUuid);

    Optional<Subscription> findByUuid(String uuid);

}
