package com.symbolic.symbolic.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import lombok.NoArgsConstructor;

/**
 * A data model for a diagnosis, representing the condition name,
 * treatment information, and diagnosis date.
 */
@Entity
@Table(name = "diagnoses")
@NoArgsConstructor
public class Diagnosis {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "cond")
  private String condition;

  @Column(name = "treatmentInfo")
  private String treatmentInfo;

  @Column(name = "date")
  @Temporal(TemporalType.DATE)
  @JsonFormat(pattern = "yyyy-MM-dd")
  private Date date;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE
      }
  )
  @JsonIgnore
  private Patient patient;

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE
      }
  )
  @JsonIgnore
  private MedicalPractitioner practitioner;

  /**
   * A constructor for the Diagnosis data model.
   *
   * @param condition     a string value describing the condition that has been diagnosed
   * @param treatmentInfo a string value describing any details about the treatment plan
   * @param date          a Date value representing when the diagnosis was performed
   */
  public Diagnosis(String condition, String treatmentInfo, Date date) {
    this.condition = condition;
    this.treatmentInfo = treatmentInfo;
    this.date = date;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public String getTreatmentInfo() {
    return treatmentInfo;
  }

  public void setTreatmentInfo(String treatmentInfo) {
    this.treatmentInfo = treatmentInfo;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public Patient getPatient() {
    return patient;
  }

  public void setPatient(Patient patient) {
    this.patient = patient;
  }

  public MedicalPractitioner getPractitioner() {
    return practitioner;
  }

  public void setPractitioner(MedicalPractitioner practitioner) {
    this.practitioner = practitioner;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Diagnosis diagnosis = (Diagnosis) o;
    return Objects.equals(id, diagnosis.id) && Objects.equals(condition, diagnosis.condition)
        && Objects.equals(treatmentInfo, diagnosis.treatmentInfo)
        && Objects.equals(date, diagnosis.date) && Objects.equals(patient, diagnosis.patient)
        && Objects.equals(practitioner, diagnosis.practitioner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, condition, treatmentInfo, date);
  }
}
