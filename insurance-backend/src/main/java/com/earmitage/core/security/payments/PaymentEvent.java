package com.earmitage.core.security.payments;

import com.earmitage.core.security.repository.Payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentEvent {

    private Payment payment;
}
