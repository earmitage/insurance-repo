package co.za.insurance.admin;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CombinedUploadRequest {
	
    private List<PolicyholderPolicyBeneficiaryUpload> data;
    private Long companyId;
}
