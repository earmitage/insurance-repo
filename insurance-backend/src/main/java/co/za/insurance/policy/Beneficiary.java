package co.za.insurance.policy;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private final String uuid = UUID.randomUUID().toString();

    @ManyToOne
    private Policy policy;

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
