package com.symbolic.symbolic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.Set;

/**
 * A data model for a medical patient, representing their vaccine history, allergy diagnoses, and accommodations.
 */
@Entity
@Table(name = "patients")
@NoArgsConstructor
public class Patient {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "vaccinations")
    private String vaccinations;
    @Column(name = "allergies")
    private String allergies;
    @Column(name = "accommodations")
    private String accommodations;

    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            mappedBy = "patients"
    )
    @JsonIgnore
    private Set<MedicalPractitioner> practitioners;

    /**
     * A constructor for the Patient data model.
     * @param vaccinations a string value representing the vaccinations the patient has received
     * @param allergies a string value representing the allergies for which the patient has been diagnosed
     * @param accommodations a string value representing any accommodations the patient has received
     */
    public Patient(String vaccinations, String allergies, String accommodations) {
        this.vaccinations = vaccinations;
        this.allergies = allergies;
        this.accommodations = accommodations;
    }

    public Long getId() {
        return id;
    }

    public String getVaccinations() {
        return vaccinations;
    }

    public void setVaccinations(String vaccinations) {
        this.vaccinations = vaccinations;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getAccommodations() {
        return accommodations;
    }

    public void setAccommodations(String accommodations) {
        this.accommodations = accommodations;
    }

    public Set<MedicalPractitioner> getPractitioners() {
        return practitioners;
    }

    public void setPractitioners(Set<MedicalPractitioner> practitioners) {
        this.practitioners = practitioners;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(id, patient.id) && Objects.equals(vaccinations, patient.vaccinations) && Objects.equals(allergies, patient.allergies) && Objects.equals(accommodations, patient.accommodations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vaccinations, allergies, accommodations);
    }
}
