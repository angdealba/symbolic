package com.symbolic.symbolic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
            fetch=FetchType.LAZY,
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
            fetch=FetchType.LAZY,
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

    /**
     * A constructor for the MedicalPractitioner data model.
     * @param latitude a double value for the latitude of the practitioner
     * @param longitude a double value for the longitude of the practitioner
     * @param specialization a string value representing the practitioner's specialization
     * @param consultationCost an integer value representing the cost of a consultation with the practitioner
     * @param yearsExperience an integer value representing the number of years experience the practitioner has
     */
    public MedicalPractitioner(Double latitude, Double longitude, String specialization, Integer consultationCost, Integer yearsExperience) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.specialization = specialization;
        this.consultationCost = consultationCost;
        this.yearsExperience = yearsExperience;
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

    public void removePatientById(Long patientId) {
        Patient patient = this.patients.stream().filter(p -> Objects.equals(p.getId(), patientId)).findFirst().orElse(null);
        if (patient != null) {
            this.patients.remove(patient);
            patient.getPractitioners().remove(this);
        }
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

    public void removeAppointmentById(Long appointmentId) {
        Appointment appointment = this.appointments.stream().filter(p -> Objects.equals(p.getId(), appointmentId)).findFirst().orElse(null);
        if (appointment != null) {
            this.appointments.remove(appointment);
            appointment.setPractitioner(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalPractitioner that = (MedicalPractitioner) o;
        return Objects.equals(id, that.id) && Objects.equals(longitude, that.longitude) && Objects.equals(latitude, that.latitude) && Objects.equals(specialization, that.specialization) && Objects.equals(consultationCost, that.consultationCost) && Objects.equals(yearsExperience, that.yearsExperience);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, longitude, latitude, specialization, consultationCost, yearsExperience);
    }
}
