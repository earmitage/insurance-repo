package com.earmitage.core.security.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.earmitage.core.security.repository.Product;
import com.earmitage.core.security.repository.Subscription;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubscriptionDto {

    private String uuid;
    private LocalDateTime subscriptionDate;
    private String username;
    private Product product;
    private List<String> paymentUuids;
    private LocalDateTime subscriptionExpiryDate;
    private Boolean active;

    public SubscriptionDto(Subscription subscription) {
        this.uuid = subscription.getUuid();
        this.subscriptionDate = subscription.getSubscriptionDate();
        this.subscriptionExpiryDate = subscription.getSubscriptionExpiryDate();
        this.active = LocalDateTime.now().isBefore(subscription.getSubscriptionExpiryDate());
        this.product = subscription.getProduct();
        this.username = subscription.getUser().getUsername();
        if (subscription.getPayments() != null) {
            this.paymentUuids = subscription.getPayments().stream().map(a ->a.getUuid()).toList();
        }
    }
}
