package com.earmitage.core.security.payments;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.earmitage.core.security.repository.Payment;
import com.earmitage.core.security.repository.PaymentRepository;
import com.earmitage.core.security.repository.PaymentStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PayFastService {

    @Autowired
    private PayFastProperties payFastProperties;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    

    public ResponseEntity<String> handlePaymentNotification(MultiValueMap<String, String> formData,
            String refererHeader) {
        if (!PayFastValidator.isRequestFromValidPayFastIP(refererHeader, payFastProperties.getDomains())) {
            log.info("error with refererHeader {}", refererHeader);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid source");
        }
/*TODO USE PAYFAST ORDER
        if (!PayFastValidator.isValidSignatureFromForm(formData, payFastProperties.getSecretKey())) {
            log.info("errro with payFastProperties.getSecretKey() {}", payFastProperties.getSecretKey());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
        }
        */

        String pfParamString = PayFastValidator.buildPfParamString(formData, payFastProperties.getSecretKey());
        if (!PayFastValidator.isValidServerConfirmation(pfParamString, payFastProperties.getValidationUrl())) {
            log.error("error with pfParamString() {}", pfParamString);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not recognized");
        }

        ITNPayload payload = PayFastMapper.mapToPayload(formData);

        Optional<Payment> optionalPayment = paymentRepository.findByUuid(payload.getMPaymentId());
      
        if (optionalPayment.isEmpty()) {
            log.error("did not find payment", optionalPayment);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment not found");
        }

        Payment payment = optionalPayment.get();

        updatePaymentFromPayload(payment, payload);
        paymentRepository.save(payment);
        log.info("dispatching for {}", pfParamString);
        eventPublisher.publishEvent(new PaymentEvent(payment));

        return ResponseEntity.ok("success");
    }

    private void updatePaymentFromPayload(Payment payment, ITNPayload payload) {
        if ("COMPLETE".equalsIgnoreCase(payload.getPaymentStatus())) {
            payment.setStatus(PaymentStatus.COMPLETED);
        } else if ("CANCELLED".equalsIgnoreCase(payload.getPaymentStatus())) {
            payment.setStatus(PaymentStatus.FAILED);
        }

        payment.setPaidAmount(payload.getAmountGross());
        payment.setAmountFee(payload.getAmountFee());
        payment.setAmountNet(payload.getAmountNet());
        payment.setPaymentGatewayId(payload.getPfPaymentId());
        payment.setPaymentTime(LocalDateTime.now());
    }
}
