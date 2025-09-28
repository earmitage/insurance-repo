package com.earmitage.core.security.payments;

import static java.lang.String.format;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.earmitage.core.security.dto.NotificationType;
import com.earmitage.core.security.notifications.AppProperties;
import com.earmitage.core.security.notifications.MailerSender;
import com.earmitage.core.security.notifications.NotificationProperties;
import com.earmitage.core.security.notifications.NotificationsDto;
import com.earmitage.core.security.notifications.NotificationsService;
import com.earmitage.core.security.repository.Payment;
import com.earmitage.core.security.repository.PaymentStatus;
import com.earmitage.core.security.repository.Subscription;
import com.earmitage.core.security.repository.SubscriptionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductDispatcher {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private NotificationsService notificationsService;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private MailerSender mailerSender;

    @EventListener
    public void handlePaymentEvent(PaymentEvent event) {
        Payment payment = event.getPayment();
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            Subscription subscription = new Subscription();
            subscription.setProduct(payment.getProduct());
            subscription.setSubscriptionDate(LocalDateTime.now());
            subscription.setSubscriptionExpiryDate(LocalDateTime.now().plusYears(1));
            subscription.setUser(payment.getUser());
            subscription.setAmountPaid(payment.getAmount());
            subscriptionRepository.save(subscription);
            log.info("{} activated subscription ", event.getPayment().getUser().getUsername());

            String message = format(NotificationProperties.NotificationContent.USER_SUBSCRIBED.getSms(),
                    event.getPayment().getUser().getUsername(),
                    appProperties.getNotifications().getNotificationsAppName());

            NotificationsDto notification = new NotificationsDto();
            notification.setAppName(appProperties.getNotifications().getNotificationsAppName());
            notification.setMessage(message);
            notification.setPhoneNumber(appProperties.getInsdeployContact());
            notification.setType(NotificationType.SMS);

            notificationsService.createNotification(notification);

            Map<String, Object> model = new HashMap<>();
            model.put("customerName", payment.getUser().getFirstname());
            model.put("plan", "Annual Policyholder Protection Plan");
            model.put("startDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            model.put("renewalDate",
                    subscription.getSubscriptionExpiryDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            model.put("amount", formatZAR(payment.getAmount()));

            try {
                ClassPathResource imgFile = new ClassPathResource("images/logo.png");
                try (InputStream is = imgFile.getInputStream()) {
                    byte[] bytes = IOUtils.toByteArray(is);
                    String logo = Base64.getEncoder().encodeToString(bytes);
                    model.put("logoBase64", logo);
                }
                mailerSender.sendSubscriptionConfirmation(payment.getUser().getEmail(), model);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String formatZAR(BigDecimal amount) {
        Locale southAfrica = Locale.forLanguageTag("en-ZA");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(southAfrica);

        return currencyFormatter.format(amount);
    }

}
