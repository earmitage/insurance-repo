package co.za.insurance.company;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(CompanyDto::new)
                .collect(Collectors.toList());
    }

    public Optional<CompanyDto> getCompanyById(Long id) {
        return companyRepository.findById(id)
                .map(CompanyDto::new);
    }

    public CompanyDto createCompany(CreateCompanyDto createDto) {
        validateCreateCompany(createDto);

        Company company = new Company();
        company.setName(createDto.getName());
        company.setAddress(createDto.getAddress());
        company.setCity(createDto.getCity());
        company.setRegion(createDto.getRegion());
        company.setPostalCode(createDto.getPostalCode());
        company.setCountry(createDto.getCountry());
        company.setPhoneNumber(createDto.getPhoneNumber());
        company.setEmail(createDto.getEmail());
        company.setWebsite(createDto.getWebsite());
        company.setRegistrationNumber(createDto.getRegistrationNumber());

        Company savedCompany = companyRepository.save(company);
        return new CompanyDto(savedCompany);
    }

    public CompanyDto updateCompany(Long id, UpdateCompanyDto updateDto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + id));

        validateUpdateCompany(company, updateDto);

        if (updateDto.getName() != null) {
            company.setName(updateDto.getName());
        }
        if (updateDto.getAddress() != null) {
            company.setAddress(updateDto.getAddress());
        }
        if (updateDto.getCity() != null) {
            company.setCity(updateDto.getCity());
        }
        if (updateDto.getRegion() != null) {
            company.setRegion(updateDto.getRegion());
        }
        if (updateDto.getPostalCode() != null) {
            company.setPostalCode(updateDto.getPostalCode());
        }
        if (updateDto.getCountry() != null) {
            company.setCountry(updateDto.getCountry());
        }
        if (updateDto.getPhoneNumber() != null) {
            company.setPhoneNumber(updateDto.getPhoneNumber());
        }
        if (updateDto.getEmail() != null) {
            company.setEmail(updateDto.getEmail());
        }
        if (updateDto.getWebsite() != null) {
            company.setWebsite(updateDto.getWebsite());
        }
        if (updateDto.getRegistrationNumber() != null) {
            company.setRegistrationNumber(updateDto.getRegistrationNumber());
        }

        Company savedCompany = companyRepository.save(company);
        return new CompanyDto(savedCompany);
    }

    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with id: " + id));

        // Check if company has associated policies
        if (company.getPolicies() != null && !company.getPolicies().isEmpty()) {
            throw new CompanyDeletionException("Cannot delete company with associated policies");
        }

        companyRepository.delete(company);
    }

    private void validateCreateCompany(CreateCompanyDto createDto) {
        if (companyRepository.existsByName(createDto.getName())) {
            throw new CompanyValidationException("Company with name '" + createDto.getName() + "' already exists");
        }

        if (createDto.getRegistrationNumber() != null &&
            !createDto.getRegistrationNumber().trim().isEmpty() &&
            companyRepository.existsByRegistrationNumber(createDto.getRegistrationNumber())) {
            throw new CompanyValidationException("Company with registration number '" + createDto.getRegistrationNumber() + "' already exists");
        }
    }

    private void validateUpdateCompany(Company existingCompany, UpdateCompanyDto updateDto) {
        if (updateDto.getName() != null &&
            !updateDto.getName().equals(existingCompany.getName()) &&
            companyRepository.existsByName(updateDto.getName())) {
            throw new CompanyValidationException("Company with name '" + updateDto.getName() + "' already exists");
        }

        if (updateDto.getRegistrationNumber() != null &&
            !updateDto.getRegistrationNumber().trim().isEmpty() &&
            !updateDto.getRegistrationNumber().equals(existingCompany.getRegistrationNumber()) &&
            companyRepository.existsByRegistrationNumber(updateDto.getRegistrationNumber())) {
            throw new CompanyValidationException("Company with registration number '" + updateDto.getRegistrationNumber() + "' already exists");
        }
    }
}