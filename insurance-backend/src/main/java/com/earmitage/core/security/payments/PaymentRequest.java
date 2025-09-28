package com.earmitage.core.security.payments;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    private String subscriptionId;
    private Double amount;
}
