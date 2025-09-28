package co.za.insurance.company;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    List<Company> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByRegistrationNumber(String registrationNumber);
}