package com.earmitage.core.security.repository;


import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private final String uuid = UUID.randomUUID().toString();

    @ManyToOne
    private User user;
    private String filename;

    private String type;  // e.g., ID Document, Passport, Receipt

    private String contentType;  // MIME type, e.g., image/jpeg

    private Long size;  // In bytes

    @Lob
    private byte[] data; 
}
