package com.earmitage.core.security.notifications;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationProperties {

    private String smsUrl;
    private String tokenId;
    private String tokenSecret;
    private boolean smsEnabled;
    private boolean emailEnabled;
    private String fromEmail;
    private String mailHost;
    private String mailUser;
    private String mailPassword;
    private int mailPort;
    private String signupUrl;
    private String notificationsAppName;

    private String twilioAccountSid;
    private String twilioAuthToken;
    private String twilioSourceNumber;
    
    private String mailerSendApi;

    @Getter
    public enum NotificationContent {

        REGISTRATION_TOKEN("Your %s registration OTP is: %s. This token expires on: %s.",
                "Your %s registration OTP is :%s This token expires on: %s"),

        PASSWORD_RESET_TOKEN("Please find your password reset token:%s \nThis OTP expires at:%s",
                "Please find your password reset token:%s <br />This OTP expires on: %s"),
        USER_SUBSCRIBED("New user %s subscribed on %s.", "New user %s subscribed on %s.");

        private String sms;
        private String email;

        NotificationContent(String sms, String email) {
            this.sms = sms;
            this.email = email;
        }
    }

}
