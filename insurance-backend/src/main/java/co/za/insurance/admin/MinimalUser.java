package co.za.insurance.admin;

import java.util.List;

import com.earmitage.core.security.dto.UserDto;
import com.earmitage.core.security.repository.Subscription;
import com.earmitage.core.security.repository.User;

import co.za.insurance.policy.AddPolicy;
import co.za.insurance.policy.Policy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MinimalUser extends UserDto {

    private List<AddPolicy> policies;
    private Boolean deceased;

    public MinimalUser(User user, final List<Policy> policies, List<Subscription> subscriptions) {
        super(user);
        this.policies = policies.stream().map(AddPolicy::new).toList();
        setSubscriptions(subscriptions);
        this.deceased = this.policies.stream().anyMatch(p ->p.getDeceased() != null && p.getDeceased());
    }

}