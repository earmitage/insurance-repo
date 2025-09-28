package com.earmitage.core.security.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.earmitage.core.security.dto.NotificationType;

public interface NotificationsRepository extends JpaRepository<Notifications, Long> {

    List<Notifications> findByUsernameAndDateCreatedBetweenAndReadFlag(String username, LocalDateTime dateFrom,
            LocalDateTime dateTo, boolean readFlag);

    List<Notifications> findByDateCreatedBetweenAndReadFlag(LocalDateTime dateFrom, LocalDateTime dateTo,
            boolean readFlag);

    List<Notifications> findByDateCreatedBetweenAndReadFlagAndType(LocalDateTime dateFrom, LocalDateTime dateTo,
            boolean readFlag, final NotificationType type);

    List<Notifications> findByUsernameAndDateCreatedBetween(String username, LocalDateTime dateFrom,
            LocalDateTime dateTo);
}
