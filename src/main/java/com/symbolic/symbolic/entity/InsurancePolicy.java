package com.symbolic.symbolic.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.NoArgsConstructor;

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
      fetch = FetchType.LAZY,
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
   * A constructor for the InsurancePolicy.
   *
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

  /**
   * Removes a patient link from the policy.
   *
   * @param patientId the id of the patient to be removed.
   */
  public void removePatientById(Long patientId) {
    Patient patient = this.patients.stream().filter(p ->
        Objects.equals(p.getId(), patientId)).findFirst().orElse(null);
    if (patient != null) {
      this.patients.remove(patient);
      patient.setInsurancePolicy(null);
    }
  }

  public Set<Patient> getPatients() {
    return patients;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InsurancePolicy that = (InsurancePolicy) o;
    return Objects.equals(id, that.id) && Objects.equals(premiumCost, that.premiumCost)
        && Objects.equals(patients, that.patients);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, premiumCost);
  }
}
