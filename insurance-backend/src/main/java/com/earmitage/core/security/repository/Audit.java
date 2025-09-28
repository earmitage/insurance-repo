package com.earmitage.core.security.repository;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entityId;

    private String actionedBy;
    private LocalDateTime actionedDate;
    private String paymentUuid;
    private String action;
    private String description;

    public static Audit audit(final String entityId, final String actionedBy, final String paymentUuid,
            final String action, final String description) {
        Audit audit = new Audit();
        audit.setAction(action);
        audit.setActionedBy(actionedBy);
        audit.setActionedDate(LocalDateTime.now());
        audit.setDescription(description);
        audit.setEntityId(entityId);
        audit.setPaymentUuid(paymentUuid);
        return audit;
    }

}
