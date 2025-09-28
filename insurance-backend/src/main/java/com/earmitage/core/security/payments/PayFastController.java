package com.earmitage.core.security.payments;

import static java.lang.String.format;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Principal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.earmitage.core.security.repository.Payment;
import com.earmitage.core.security.repository.PaymentRepository;
import com.earmitage.core.security.repository.PaymentStatus;
import com.earmitage.core.security.repository.Product;
import com.earmitage.core.security.repository.ProductRepository;
import com.earmitage.core.security.repository.SubscriptionFrequency;
import com.earmitage.core.security.repository.User;
import com.earmitage.core.security.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@RestController
public class PayFastController {

    @Autowired
    private PayFastProperties payFastProperties;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PayFastService payFastService;

    DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.of("en", "ZA")));

    @PostMapping("${app.url}/payments/initiations/")
    public ResponseEntity<RedirectResponse> initiatePayment(@RequestBody OrderDetails orderDetails,
            Principal principal) {
        Product product = productRepository.findByUuid(orderDetails.getProductUuid())
                .orElseThrow(() ->new ProductNotFoundException(orderDetails.getProductUuid()));

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() ->new UsernameNotFoundException("Must be logged in to make payments"));

        Payment payment = new Payment();
        if (product.getFrequency() == SubscriptionFrequency.ANNUAL) {
            payment.setAmount(product.getAnnualCost());
        }
        if (product.getFrequency() == SubscriptionFrequency.MONTHLY) {
            payment.setAmount(product.getMonthlyCost());
        }
        payment.setUser(user);
        payment.setProduct(product);
        payment.setCurrency(orderDetails.getCurrency());
        payment.setStatus(PaymentStatus.INITIATED);

        LinkedHashMap<String, String> paymentData = new LinkedHashMap<>();
        paymentData.put("merchant_id", payFastProperties.getMerchantId());
        paymentData.put("merchant_key", payFastProperties.getMerchantKey());

        paymentData.put("return_url", payFastProperties.getReplyUrl());

        paymentData.put("cancel_url", format("%s%s", payFastProperties.getBaseAppUrl(), "unsecured/payments/cancellations/"));
        paymentData.put("notify_url", format("%s%s", payFastProperties.getBaseAppUrl(), "unsecured/payments/notifications/"));

        paymentData.put("m_payment_id", payment.getUuid());

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);

        paymentData.put("amount", df.format(payment.getAmount()));
        paymentData.put("item_name", product.getName());
        paymentData.put("item_description", product.getDescription());

        String signature = generatePayFastSignature(paymentData);
        payment.setSignature(signature);
        paymentRepository.save(payment);
        paymentData.put("signature", signature);

        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            paymentData.forEach(formData::add);

            StringBuilder query = new StringBuilder();
            for (Map.Entry<String, String> entry : paymentData.entrySet()) {
                query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)).append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
            }
            String redirectUrl = payFastProperties.getPayfastUrl() + "?" + query.toString();

            return ResponseEntity.ok(new RedirectResponse(redirectUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RedirectResponse(e.getMessage()));
        }
    }

    @PostMapping("${app.url}/unsecured/payments/notifications/")
    public ResponseEntity<String> notifyPayment(@RequestParam MultiValueMap<String, String> formData,
            HttpServletRequest request) {
        return payFastService.handlePaymentNotification(formData, request.getHeader("referer"));
    }

    @PostMapping("${app.url}/unsecured/payments/cancellations/")
    public ResponseEntity<String> cancelPayment(@RequestParam MultiValueMap<String, String> formData,
            HttpServletRequest request) {

        return payFastService.handlePaymentNotification(formData, request.getHeader("referer"));
    }

    private String generatePayFastSignature(LinkedHashMap<String, String> data) {
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                String value = entry.getValue();
                if (value != null && !value.trim().isEmpty()) {
                    sb.append(entry.getKey()).append("=").append(URLEncoder.encode(value.trim(), "UTF-8")).append("&");
                }
            }

            // Remove trailing ampersand
            String signatureString = sb.substring(0, sb.length() - 1);

            // Append passphrase if present
            if (payFastProperties.getSecretKey() != null && !payFastProperties.getSecretKey().trim().isEmpty()) {
                signatureString += "&passphrase=" + URLEncoder.encode(payFastProperties.getSecretKey().trim(), "UTF-8");
            }
            // Generate MD5 hash
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(signatureString.getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, digest));
        } catch (Exception e) {
            throw new RuntimeException("Error generating PayFast signature", e);
        }
    }

    @Getter
    @Setter
    public static class OrderDetails {

        private double amount;
        private String currency;
        private String productUuid;
    }

    @Getter
    @Setter
    public static class RedirectResponse {

        private String redirectUrl;

        public RedirectResponse(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
    }
}
