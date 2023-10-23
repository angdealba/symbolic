package com.symbolic.symbolic.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A data model for an insurance policy, representing its premium cost.
 */
@Entity
@Table(name = "insurance_policies")
@NoArgsConstructor
public class InsurancePolicy {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "premiumCost")
    private Integer premiumCost;

    @OneToMany(
            fetch=FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    @JoinTable(
            name = "policy_patients",
            joinColumns = @JoinColumn(name = "policy_id"),
            inverseJoinColumns = @JoinColumn(name = "patient_id")
    )
    private Set<Patient> patients = new HashSet<>();

    /**
     * A constructor for the InsurancePolicy
     * @param premiumCost an integer value representing the cost of the insurance premium
     */
    public InsurancePolicy(Integer premiumCost) {
        this.premiumCost = premiumCost;
    }

    public Long getId() {
        return id;
    }

    public Integer getPremiumCost() {
        return premiumCost;
    }

    public void setPremiumCost(Integer premiumCost) {
        this.premiumCost = premiumCost;
    }

    public void addPatient(Patient patient) {
        this.patients.add(patient);
        patient.setInsurancePolicy(this);
    }

    public void removePatientById(Long patientId) {
        Patient patient = this.patients.stream().filter(p -> Objects.equals(p.getId(), patientId)).findFirst().orElse(null);
        if (patient != null) {
            this.patients.remove(patient);
            patient.setInsurancePolicy(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InsurancePolicy that = (InsurancePolicy) o;
        return Objects.equals(id, that.id) && Objects.equals(premiumCost, that.premiumCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, premiumCost);
    }
}
