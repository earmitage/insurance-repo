package com.earmitage.core.security.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private final String uuid = UUID.randomUUID().toString();

    private LocalDateTime subscriptionDate;
    private LocalDateTime subscriptionExpiryDate;

    @ManyToOne
    private User user;

    @ManyToOne
    private Product product;
    
    @OneToMany
    private List<Payment> payments;
    
    private BigDecimal amountPaid;

}
