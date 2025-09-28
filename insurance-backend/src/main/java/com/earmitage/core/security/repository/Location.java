package com.earmitage.core.security.repository;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
public class Location implements Serializable {

    private String name;
    private Double latitude;
    private Double longitude;
    private String address;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String region;
    private String country;
    private String postalCode;

    public Location() {
    }

    public Location(final String name, final double latitude, final double longitude, final String address,
            final String city, final String region) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.city = city;
        this.region = region;
    }

    public Location(final String name) {
        this.name = name;
    }

}
