package com.earmitage.core.security.notifications;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class EmailNotification {

    private String subject;
    private String from;
    private String to;
    private String message;
    private String recipientName;
}
