package com.symbolic.symbolic.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.data.util.Pair;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "specialization")
    private String specialization;

    @ManyToMany(
            fetch=FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    @JoinTable(
            name = "facility_patients",
            joinColumns = @JoinColumn(name = "facility_id"),
            inverseJoinColumns = @JoinColumn(name = "patient_id")
    )
    private Set<Patient> patients = new HashSet<>();

    @OneToMany(
            fetch=FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    @JoinTable(
            name = "facility_practitioners",
            joinColumns = @JoinColumn(name = "facility_id"),
            inverseJoinColumns = @JoinColumn(name = "practitioner_id")
    )
    private Set<MedicalPractitioner> practitioners = new HashSet<>();

    @OneToMany(
            fetch=FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    @JoinTable(
            name = "facility_appointments",
            joinColumns = @JoinColumn(name = "facility_id"),
            inverseJoinColumns = @JoinColumn(name = "appointment_id")
    )
    private Set<Appointment> appointments = new HashSet<>();

    /**
     * A constructor for the Facility data model.
     * @param latitude a double value for the latitude of the facility
     * @param longitude a double value for the longitude of the facility
     * @param specialization a string value representing the specialization at the facility
     */
    public Facility(Double latitude, Double longitude, String specialization) {
        this.latitude = latitude;
        this.longitude = longitude;
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

    public void addPatient(Patient patient) {
        this.patients.add(patient);
        patient.getFacilities().add(this);
    }

    public void removePatientById(Long patientId) {
        Patient patient = this.patients.stream().filter(p -> Objects.equals(p.getId(), patientId)).findFirst().orElse(null);
        if (patient != null) {
            this.patients.remove(patient);
            patient.getFacilities().remove(this);
        }
    }

    public Set<Patient> getPatients() {
        return patients;
    }

    public void addPractitioner(MedicalPractitioner practitioner) {
        this.practitioners.add(practitioner);
        practitioner.setFacility(this);
    }

    public void removePractitionerById(Long practitionerId) {
        MedicalPractitioner practitioner = this.practitioners.stream().filter(p -> Objects.equals(p.getId(), practitionerId)).findFirst().orElse(null);
        if (practitioner != null) {
            this.practitioners.remove(practitioner);
            practitioner.setFacility(null);
        }
    }

    public Set<MedicalPractitioner> getPractitioners() {
        return practitioners;
    }

    public void addAppointment(Appointment appointment) {
        this.appointments.add(appointment);
        appointment.setFacility(this);
    }

    public void removeAppointmentById(Long appointmentId) {
        Appointment appointment = this.appointments.stream().filter(p -> Objects.equals(p.getId(), appointmentId)).findFirst().orElse(null);
        if (appointment != null) {
            this.appointments.remove(appointment);
            appointment.setFacility(null);
        }
    }

    public Set<Appointment> getAppointments() {
        return appointments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Facility facility = (Facility) o;
        return Objects.equals(id, facility.id) && Objects.equals(latitude, facility.latitude) && Objects.equals(longitude, facility.longitude) && Objects.equals(specialization, facility.specialization) && Objects.equals(patients, facility.patients) && Objects.equals(practitioners, facility.practitioners) && Objects.equals(appointments, facility.appointments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, latitude, longitude, specialization);
    }
}
