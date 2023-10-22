package com.symbolic.symbolic.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import java.util.Objects;

/**
 * A data model for a medical practitioner, representing their location, specialization, consultation cost, and years experience.
 */
@Entity
@Table(name = "practitioners")
@NoArgsConstructor
public class MedicalPractitioner {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "location")
    private Point location;
    @Column(name = "specialization")
    private String specialization;
    @Column(name = "consultation_cost")
    private Integer consultation_cost;

    @Column(name = "years_experience")
    private Integer years_experience;

    /**
     * A constructor for the MedicalPractitioner data model.
     * @param location a Point object representing the latitude/longitude coordinates of the practitioner
     * @param specialization a string value representing the practitioner's specialization
     * @param consultation_cost an integer value representing the cost of a consultation with the practitioner
     * @param years_experience an integer value representing the number of years experience the practitioner has
     */
    public MedicalPractitioner(Point location, String specialization, Integer consultation_cost, Integer years_experience) {
        this.location = location;
        this.specialization = specialization;
        this.consultation_cost = consultation_cost;
        this.years_experience = years_experience;
    }

    public Long getId() {
        return id;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getConsultation_cost() {
        return consultation_cost;
    }

    public void setConsultation_cost(Integer consultation_cost) {
        this.consultation_cost = consultation_cost;
    }

    public Integer getYears_experience() {
        return years_experience;
    }

    public void setYears_experience(Integer years_experience) {
        this.years_experience = years_experience;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalPractitioner that = (MedicalPractitioner) o;
        return Objects.equals(id, that.id) && Objects.equals(location, that.location) && Objects.equals(specialization, that.specialization) && Objects.equals(consultation_cost, that.consultation_cost) && Objects.equals(years_experience, that.years_experience);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location, specialization, consultation_cost, years_experience);
    }
}
