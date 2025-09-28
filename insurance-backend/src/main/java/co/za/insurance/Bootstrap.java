package co.za.insurance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import com.earmitage.core.security.dto.ContactType;
import com.earmitage.core.security.notifications.AppProperties;
import com.earmitage.core.security.repository.Product;
import com.earmitage.core.security.repository.ProductRepository;
import com.earmitage.core.security.repository.RoleRepository;
import com.earmitage.core.security.repository.SubscriptionFrequency;
import com.earmitage.core.security.repository.User;
import com.earmitage.core.security.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class Bootstrap {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppProperties appProperties;
    
    @EventListener(ApplicationReadyEvent.class)
    public void bootstrap() {
        if (roleRepository.count() == 0) {
            for (Role role : Role.values()) {
                roleRepository.save(new com.earmitage.core.security.repository.Role(role.name()));
            }
        }
        if (productRepository.count() == 0) {
            Product product = new Product();
            product.setName("Annual Subscription");
            product.setFrequency(SubscriptionFrequency.ANNUAL);
            product.setDescription("Premium subscription");
            product.setAnnualCost(BigDecimal.valueOf(85.00));
            productRepository.save(product);
        }

        if (userRepository.count() == 0) {
            User entity = new User("evans", "r035198x", "E", "A", "r035198x@gmail.com", "+27000000000", LocalDate.now());
            entity.setRoles(Set.of(roleRepository.findByName(Role.ROLE_POLICY_HOLDER.name())));
            entity.setActive(true);
            entity.setLocked(false);
            entity.setEnabled(true);
            entity.setIdNumber("8434567890123");
            entity.setContactType(ContactType.SMS);
            userRepository.save(entity);
            log.info("Added user");

            User admin = new User(appProperties.getInsdeployAdmin(), appProperties.getInsdeployToken(), "Admin",
                    "Admin", "Admin", appProperties.getInsdeployContact(), LocalDate.now());
            admin.setRoles(Set.of(roleRepository.findByName(Role.ROLE_ADMIN.name())));
            admin.setActive(true);
            admin.setLocked(false);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("Added admin");

        }
        else {
          userRepository.findByUsername(appProperties.getInsdeployAdmin()).ifPresent(user -> {
              user.resetPassword(appProperties.getInsdeployToken());
              userRepository.save(user);
          });
        }

    }
}
