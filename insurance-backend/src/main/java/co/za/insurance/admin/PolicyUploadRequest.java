package co.za.insurance.admin;

import java.util.List;

import co.za.insurance.policy.AddPolicy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PolicyUploadRequest {
    private List<AddPolicy> data;
    private Long companyId;
}