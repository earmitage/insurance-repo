package co.za.insurance.policy;

import java.util.List;

import com.earmitage.core.security.repository.Subscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetPolicies {

    List<AddPolicy> policies;
    private List<Subscription> activeSubcriptions;
}
