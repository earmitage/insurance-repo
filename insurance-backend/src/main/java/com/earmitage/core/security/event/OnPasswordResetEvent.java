package com.earmitage.core.security.event;

import com.earmitage.core.security.repository.PasswordResetToken;
import com.earmitage.core.security.repository.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@SuppressWarnings("serial")
@Getter
@AllArgsConstructor
public class OnPasswordResetEvent {

    private final User user;
    private final PasswordResetToken token;
}
