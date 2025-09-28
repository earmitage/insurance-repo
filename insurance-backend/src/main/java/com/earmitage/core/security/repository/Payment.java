package com.earmitage.core.security.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private final String uuid = UUID.randomUUID().toString();

    private LocalDateTime paymentTime;

    @ManyToOne
    private User user;

    private BigDecimal amount;
    private BigDecimal paidAmount;
    private String currency;
    private String paymentGatewayId;

    @ManyToOne
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    private BigDecimal amountGross;
    private BigDecimal amountFee;
    private BigDecimal amountNet;
    
    private String signature;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private Product product;

}
