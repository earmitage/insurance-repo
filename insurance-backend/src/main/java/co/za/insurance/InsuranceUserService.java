package co.za.insurance;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.earmitage.core.security.CustomUserService;
import com.earmitage.core.security.dto.RegistrationRequest;
import com.earmitage.core.security.repository.RoleRepository;
import com.earmitage.core.security.repository.User;

@Service
public class InsuranceUserService implements CustomUserService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public User addRoles(User user, RegistrationRequest request) {
        user.setRoles(Set.of(roleRepository.findByName(request.getRole())));
        return user;
    }

}
