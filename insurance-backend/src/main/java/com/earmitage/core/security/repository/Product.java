package com.earmitage.core.security.repository;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private final String uuid = UUID.randomUUID().toString();

    private String name;
    private String description;
    private BigDecimal monthlyCost;
    private BigDecimal annualCost;

    @Enumerated(EnumType.STRING)
    private SubscriptionFrequency frequency;

}
