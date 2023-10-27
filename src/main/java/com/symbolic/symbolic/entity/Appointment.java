package com.symbolic.symbolic.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import java.util.Objects;
import lombok.NoArgsConstructor;

/**
 * A data model for an appointment, representing its time and cost.
 */
@Entity
@Table(name = "appointments")
@NoArgsConstructor
public class Appointment {

  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "dateTime")
  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
  private Date dateTime;

  @Column(name = "cost")
  private Integer cost;

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

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE
      }
  )
  @JsonIgnore
  private Facility facility;

  /**
   * A constructor for the Appointment data model.
   *
   * @param dateTime a Date value representing the appointment time
   * @param cost     an integer value representing the cost of the appointment
   */
  public Appointment(Date dateTime, Integer cost) {
    this.dateTime = dateTime;
    this.cost = cost;
  }

  public Long getId() {
    return id;
  }

  public Date getDateTime() {
    return dateTime;
  }

  public void setDateTime(Date dateTime) {
    this.dateTime = dateTime;
  }

  public Integer getCost() {
    return cost;
  }

  public void setCost(Integer cost) {
    this.cost = cost;
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

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Appointment that = (Appointment) o;
    return Objects.equals(id, that.id) && Objects.equals(dateTime, that.dateTime)
        && Objects.equals(cost, that.cost) && Objects.equals(patient, that.patient)
        && Objects.equals(practitioner, that.practitioner)
        && Objects.equals(facility, that.facility);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, dateTime, cost);
  }
}
