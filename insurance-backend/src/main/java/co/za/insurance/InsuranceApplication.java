package co.za.insurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.earmitage.core.security.OAuthConfig;
import com.earmitage.core.security.UserService;

@SpringBootApplication
@EntityScan(basePackageClasses = { InsuranceApplication.class, UserService.class })
@EnableJpaRepositories(basePackageClasses = { InsuranceApplication.class, UserService.class })
@ComponentScan(basePackageClasses = { InsuranceApplication.class, OAuthConfig.class })
public class InsuranceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceApplication.class, args);
    }
}
