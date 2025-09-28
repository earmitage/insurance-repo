package com.earmitage.core.security.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit, Long> {

    List<Audit> findByActionedByAndActionedDateBetween(String actionedBy, LocalDateTime start, LocalDateTime end);

    List<Audit> findByEntityIdAndActionedDateBetween(String papssId, LocalDateTime start, LocalDateTime end);

    List<Audit> findByPaymentUuid(String paymentUuid);

}
