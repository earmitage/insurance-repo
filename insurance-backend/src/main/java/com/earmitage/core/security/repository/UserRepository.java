package com.earmitage.core.security.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    User findByUsernameAndActiveTrue(String username);

    User findByEmail(String email);

    User findByPhone(String phone);

    Optional<User> findByUsernameAndActiveTrueAndEnabledTrue(String username);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update User u set u.active = true where u.username =:username")
    void activate(@Param("username") String username);

    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findUsersByRole(@Param("roleName") String roleName);
    
    @Query("SELECT u FROM User u WHERE u.phone = :phone OR u.email = :email")
    User findByPhoneOrEmail(String phone, String email);
}
