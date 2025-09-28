package com.earmitage.core.security.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    
    List<Image> findByUserUsername(String username);
    
     Image findByUuid(String uuid);
}

