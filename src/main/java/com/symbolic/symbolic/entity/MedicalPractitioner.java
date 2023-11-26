package com.symbolic.symbolic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.NoArgsConstructor;
import org.springframework.data.util.Pair;



/**
 * A data model for a medical practitioner, representing their location, specialization,
 * consultation cost, and years experience.
 */
@Entity
@Table(name = "practitioners")
@NoArgsConstructor
public class MedicalPractitioner {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "latitude")
  private Double latitude;
  @Column(name = "longitude")
  private Double longitude;
  @Column(name = "specialization")
  private String specialization;
  @Column(name = "consultationCost")
  private Integer consultationCost;

  @Column(name = "yearsExperience")
  private Integer yearsExperience;

  @ManyToMany(
      fetch = FetchType.LAZY,
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE
      }
  )
  @JoinTable(
      name = "practitioner_patients",
      joinColumns = @JoinColumn(name = "practitioner_id"),
      inverseJoinColumns = @JoinColumn(name = "patient_id")
  )
  private Set<Patient> patients = new HashSet<Patient>();

  @ManyToOne(
      fetch = FetchType.LAZY,
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE
      }
  )
  @JsonIgnore
  private Facility facility;

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE
      }
  )
  @JoinTable(
      name = "practitioner_appointments",
      joinColumns = @JoinColumn(name = "practitioner_id"),
      inverseJoinColumns = @JoinColumn(name = "appointment_id")
  )
  private Set<Appointment> appointments = new HashSet<>();

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE
      }
  )
  @JoinTable(
      name = "practitioner_prescriptions",
      joinColumns = @JoinColumn(name = "practitioner_id"),
      inverseJoinColumns = @JoinColumn(name = "prescription_id")
  )
  private Set<Prescription> prescriptions = new HashSet<>();

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = {
          CascadeType.PERSIST,
          CascadeType.MERGE
      }
  )
  @JoinTable(
      name = "practitioner_diagnoses",
      joinColumns = @JoinColumn(name = "practitioner_id"),
      inverseJoinColumns = @JoinColumn(name = "diagnoses_id")
  )
  private Set<Diagnosis> diagnoses = new HashSet<>();

  /**
   * A constructor for the MedicalPractitioner data model.
   *
   * @param latitude         a double value for the latitude of the practitioner
   * @param longitude        a double value for the longitude of the practitioner
   * @param specialization   a string value representing the practitioner's specialization
   * @param consultationCost an integer value representing the cost of a consultation
   * @param yearsExperience  an integer value representing their number of years experience
   */
  public MedicalPractitioner(Double latitude, Double longitude, String specialization,
                             Integer consultationCost, Integer yearsExperience) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.specialization = specialization;
    this.consultationCost = consultationCost;
    this.yearsExperience = yearsExperience;
  }

  public UUID getId() {
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

  public Pair<Double, Double> getLocation() {
    return Pair.of(this.latitude, this.longitude);
  }

  public void setLocation(Pair<Double, Double> coordinates) {
    this.setLatitude(coordinates.getFirst());
    this.setLongitude(coordinates.getSecond());
  }

  public String getSpecialization() {
    return specialization;
  }

  public void setSpecialization(String specialization) {
    this.specialization = specialization;
  }

  public Integer getConsultationCost() {
    return consultationCost;
  }

  public void setConsultationCost(Integer consultationCost) {
    this.consultationCost = consultationCost;
  }

  public Integer getYearsExperience() {
    return yearsExperience;
  }

  public void setYearsExperience(Integer yearsExperience) {
    this.yearsExperience = yearsExperience;
  }

  public void addPatient(Patient patient) {
    this.patients.add(patient);
    patient.getPractitioners().add(this);
  }

  /**
   * Removes a patient link from the practitioner.
   *
   * @param patientId the id of the patient to be removed.
   */
  public void removePatientById(UUID patientId) {
    Patient patient = this.patients.stream().filter(p ->
        Objects.equals(p.getId(), patientId)).findFirst().orElse(null);
    if (patient != null) {
      this.patients.remove(patient);
      //patient.getPractitioners().remove(this);
    }
  }

  public Set<Patient> getPatients() {
    return patients;
  }

  public Facility getFacility() {
    return facility;
  }

  public void setFacility(Facility facility) {
    this.facility = facility;
  }

  public void addAppointment(Appointment appointment) {
    this.appointments.add(appointment);
    appointment.setPractitioner(this);
  }

  /**
   * Removes an appointment link from the practitioner.
   *
   * @param appointmentId the id of the appointment to be removed.
   */
  public void removeAppointmentById(UUID appointmentId) {
    Appointment appointment = this.appointments.stream().filter(p ->
        Objects.equals(p.getId(), appointmentId)).findFirst().orElse(null);
    if (appointment != null) {
      this.appointments.remove(appointment);
      appointment.setPractitioner(null);
    }
  }

  public Set<Appointment> getAppointments() {
    return appointments;
  }

  public void addPrescription(Prescription prescription) {
    this.prescriptions.add(prescription);
    prescription.setPractitioner(this);
  }

  /**
   * Removes a prescription link from the practitioner.
   *
   * @param prescriptionId the id of the prescription to be removed.
   */
  public void removePrescriptionById(UUID prescriptionId) {
    Prescription prescription = this.prescriptions.stream().filter(p ->
        Objects.equals(p.getId(), prescriptionId)).findFirst().orElse(null);
    if (prescription != null) {
      this.prescriptions.remove(prescription);
      prescription.setPractitioner(null);
    }
  }

  public Set<Prescription> getPrescriptions() {
    return prescriptions;
  }

  public void addDiagnosis(Diagnosis diagnosis) {
    this.diagnoses.add(diagnosis);
    diagnosis.setPractitioner(this);
  }

  /**
   * Removes a diagnosis link from the practitioner.
   *
   * @param diagnosisId the id of the diagnosis to be removed.
   */
  public void removeDiagnosisById(UUID diagnosisId) {
    Diagnosis diagnosis = this.diagnoses.stream().filter(p ->
        Objects.equals(p.getId(), diagnosisId)).findFirst().orElse(null);
    if (diagnosis != null) {
      this.diagnoses.remove(diagnosis);
      diagnosis.setPractitioner(null);
    }
  }

  public Set<Diagnosis> getDiagnoses() {
    return diagnoses;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MedicalPractitioner that = (MedicalPractitioner) o;
    return Objects.equals(id, that.id) && Objects.equals(longitude, that.longitude)
        && Objects.equals(latitude, that.latitude)
        && Objects.equals(specialization, that.specialization)
        && Objects.equals(consultationCost, that.consultationCost)
        && Objects.equals(yearsExperience, that.yearsExperience);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, longitude, latitude, specialization, consultationCost, yearsExperience);
  }
}
