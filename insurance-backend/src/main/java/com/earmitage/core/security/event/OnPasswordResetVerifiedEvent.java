package com.earmitage.core.security.event;

import com.earmitage.core.security.repository.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@SuppressWarnings("serial")
@Getter
@AllArgsConstructor
public class OnPasswordResetVerifiedEvent {

    private final User user;
}
