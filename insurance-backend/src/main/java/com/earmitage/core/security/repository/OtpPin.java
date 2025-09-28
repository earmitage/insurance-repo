package com.earmitage.core.security.repository;

import java.time.LocalDateTime;
import java.util.Random;

import com.earmitage.core.security.dto.OtpType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OtpPin extends BaseEntity {

    private static final String ALPHABET = "0123456789";

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "pin_seq", sequenceName = "pin_seq", allocationSize = 1)
    private Long id;

    private LocalDateTime dateCreated = LocalDateTime.now();

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private OtpType type;

    private String pin;
    private Boolean approved;
    private int retriesCount;

    public OtpPin(final User user, final OtpType type) {
        this.user = user;
        this.type = type;
        pin = generatePin(5);
        approved = false;
    }

    public static String generatePin(final int length) {
        final Random random = new Random();
        final int n = ALPHABET.length();
        final StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) {
            s.append(ALPHABET.charAt(random.nextInt(n)));
        }
        return s.toString();
    }

}
