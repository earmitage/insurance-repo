package co.za.insurance.policy;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddBeneficiary {
    
    private String uuid;
    private String fullName;
    private String idNumber;
    private String relationship;
    private LocalDate dateOfBirth;
    private Double sharePercentage;
    private String idType;
    private String countryCode;
    private String phone;
    private String email;
    private Boolean loginAllowed;
    private Boolean deceased;
    private LocalDate deceasedDate;
    
}
