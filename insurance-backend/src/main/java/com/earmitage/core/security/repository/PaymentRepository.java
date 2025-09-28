package com.earmitage.core.security.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByUuid(String uuid);

    List<Payment> findByUserUsername(String username);

    List<Payment> findBySubscriptionUuid(String uuid);

}

