package com.symbolic.symbolic.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

/**
 * A data model for a medical facility, representing their location and specialization.
 */
@Entity
@Table(name = "facilities")
@NoArgsConstructor
public class Facility {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "specialization")
    private String specialization;

    /**
     * A constructor for the Facility data model.
     * @param longitude a double value for the longitude of the facility
     * @param latitude a double value for the latitude of the facility
     * @param specialization a string value representing the specialization at the facility
     */
    public Facility(Double longitude, Double latitude, String specialization) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.specialization = specialization;
    }

    public Long getId() {
        return id;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
