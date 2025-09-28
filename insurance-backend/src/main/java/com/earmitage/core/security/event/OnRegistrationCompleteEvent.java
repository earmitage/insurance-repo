package com.earmitage.core.security.event;

import com.earmitage.core.security.dto.ContactType;
import com.earmitage.core.security.repository.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@SuppressWarnings("serial")
@Getter
@AllArgsConstructor
public class OnRegistrationCompleteEvent {

    private final User user;
    private final String role;
    private ContactType contactType;

}
