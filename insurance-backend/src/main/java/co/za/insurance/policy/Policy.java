package co.za.insurance.policy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.earmitage.core.security.repository.User;
import co.za.insurance.company.Company;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private final String uuid = UUID.randomUUID().toString();

    @ManyToOne
    private User owner;

    @ManyToOne
    private Company company;

    private String policyNumber;
    private String insuranceCompany;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
    private PolicyType policyType;
    
    private Double coverageAmount;
    private Double monthlyPremium;
    private String status;
    
    private String addressLine1;
    private String city;
    private String region;
    private String postalCode;
    private String country;
    
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Beneficiary> beneficiaries;
    
    private Boolean deceased;
    private LocalDate deceasedDate;

}