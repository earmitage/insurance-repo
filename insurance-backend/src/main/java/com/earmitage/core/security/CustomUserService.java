package com.earmitage.core.security;

import com.earmitage.core.security.dto.RegistrationRequest;
import com.earmitage.core.security.repository.User;

public interface CustomUserService {

    public User addRoles(User user, RegistrationRequest request);

}
