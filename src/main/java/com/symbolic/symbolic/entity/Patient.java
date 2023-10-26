package com.symbolic.symbolic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jdk.jshell.Diag;
import lombok.NoArgsConstructor;

import java.util.HashSet;
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
    private Set<MedicalPractitioner> practitioners = new HashSet<>();

    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            },
            mappedBy = "patients"
    )
    @JsonIgnore
    private Set<Facility> facilities = new HashSet<>();

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    @JsonIgnore
    private InsurancePolicy insurancePolicy;

    @OneToMany(
            fetch=FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    @JoinTable(
            name = "patient_appointments",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "appointment_id")
    )
    private Set<Appointment> appointments = new HashSet<>();

    @OneToMany(
            fetch=FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    @JoinTable(
            name = "patient_prescriptions",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "prescription_id")
    )
    private Set<Prescription> prescriptions = new HashSet<>();

    @OneToMany(
            fetch=FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            }
    )
    @JoinTable(
            name = "patient_diagnoses",
            joinColumns = @JoinColumn(name = "patient_id"),
            inverseJoinColumns = @JoinColumn(name = "diagnosis_id")
    )
    private Set<Diagnosis> diagnoses = new HashSet<>();

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

    public void addPractitioner(MedicalPractitioner practitioner) {
        this.practitioners.add(practitioner);
        practitioner.addPatient(this);
    }

    public void removePractitionerById(Long practitionerId) {
        MedicalPractitioner practitioner = this.practitioners.stream().filter(p -> Objects.equals(p.getId(), practitionerId)).findFirst().orElse(null);
        if (practitioner != null) {
            this.practitioners.remove(practitioner);
            practitioner.removePatientById(id);
        }
    }

    public Set<MedicalPractitioner> getPractitioners() {
        return practitioners;
    }

    public void addFacility(Facility facility) {
        this.facilities.add(facility);
        facility.addPatient(this);
    }

    public void removeFacilityById(Long facilityId) {
        Facility facility = this.facilities.stream().filter(p -> Objects.equals(p.getId(), facilityId)).findFirst().orElse(null);
        if (facility != null) {
            this.facilities.remove(facility);
            facility.removePatientById(id);
        }
    }

    public Set<Facility> getFacilities() {
        return facilities;
    }

    public InsurancePolicy getInsurancePolicy() {
        return insurancePolicy;
    }

    public void setInsurancePolicy(InsurancePolicy insurancePolicy) {
        this.insurancePolicy = insurancePolicy;
    }

    public void addAppointment(Appointment appointment) {
        this.appointments.add(appointment);
        appointment.setPatient(this);
    }

    public void removeAppointmentById(Long appointmentId) {
        Appointment appointment = this.appointments.stream().filter(p -> Objects.equals(p.getId(), appointmentId)).findFirst().orElse(null);
        if (appointment != null) {
            this.appointments.remove(appointment);
            appointment.setPatient(null);
        }
    }

    public Set<Appointment> getAppointments() {
        return appointments;
    }

    public void addPrescription(Prescription prescription) {
        this.prescriptions.add(prescription);
        prescription.setPatient(this);
    }

    public void removePrescriptionById(Long prescriptionId) {
        Prescription prescription = this.prescriptions.stream().filter(p -> Objects.equals(p.getId(), prescriptionId)).findFirst().orElse(null);
        if (prescription != null) {
            this.prescriptions.remove(prescription);
            prescription.setPatient(null);
        }
    }

    public Set<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public void addDiagnosis(Diagnosis diagnosis) {
        this.diagnoses.add(diagnosis);
        diagnosis.setPatient(this);
    }

    public void removeDiagnosisById(Long diagnosisId) {
        Diagnosis diagnosis = this.diagnoses.stream().filter(p -> Objects.equals(p.getId(), diagnosisId)).findFirst().orElse(null);
        if (diagnosis != null) {
            this.diagnoses.remove(diagnosis);
            diagnosis.setPatient(null);
        }
    }

    public Set<Diagnosis> getDiagnoses() {
        return diagnoses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(id, patient.id) && Objects.equals(vaccinations, patient.vaccinations) && Objects.equals(allergies, patient.allergies) && Objects.equals(accommodations, patient.accommodations) && Objects.equals(practitioners, patient.practitioners);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vaccinations, allergies, accommodations);
    }
}
