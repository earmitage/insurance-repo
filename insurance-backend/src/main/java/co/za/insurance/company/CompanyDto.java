package co.za.insurance.company;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanyDto {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String region;
    private String postalCode;
    private String country;
    private String phoneNumber;
    private String email;
    private String website;
    private String registrationNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CompanyDto(Company company) {
        this.id = company.getId();
        this.name = company.getName();
        this.address = company.getAddress();
        this.city = company.getCity();
        this.region = company.getRegion();
        this.postalCode = company.getPostalCode();
        this.country = company.getCountry();
        this.phoneNumber = company.getPhoneNumber();
        this.email = company.getEmail();
        this.website = company.getWebsite();
        this.registrationNumber = company.getRegistrationNumber();
        this.createdAt = company.getCreatedAt();
        this.updatedAt = company.getUpdatedAt();
    }
}