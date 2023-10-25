package com.symbolic.symbolic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * A data model for a prescription, representing its dosage, frequency, cost, and usage instructions.
 */
@Entity
@Table(name = "prescriptions")
@NoArgsConstructor
public class Prescription {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "dosage")
    private Integer dosage;

    @Column(name = "dailyUses")
    private Integer dailyUses;

    @Column(name = "cost")
    private Integer cost;

    @Column(name = "instructions")
    private String instructions;

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
     * A constructor for the Prescription data model.
     * @param dosage an integer value representing the dosage of the medication (number of pills taken at a time)
     * @param dailyUses an integer value representing the number of times the medication should be taken daily
     * @param cost an integer value representing the cost of the prescription
     * @param instructions a string value describing any instructions for the use of the medication
     */
    public Prescription(Integer dosage, Integer dailyUses, Integer cost, String instructions) {
        this.dosage = dosage;
        this.dailyUses = dailyUses;
        this.cost = cost;
        this.instructions = instructions;
    }

    public Long getId() {
        return id;
    }

    public Integer getDosage() {
        return dosage;
    }

    public void setDosage(Integer dosage) {
        this.dosage = dosage;
    }

    public Integer getDailyUses() {
        return dailyUses;
    }

    public void setDailyUses(Integer dailyUses) {
        this.dailyUses = dailyUses;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prescription that = (Prescription) o;
        return Objects.equals(id, that.id) && Objects.equals(dosage, that.dosage) && Objects.equals(dailyUses, that.dailyUses) && Objects.equals(cost, that.cost) && Objects.equals(instructions, that.instructions) && Objects.equals(patient, that.patient) && Objects.equals(practitioner, that.practitioner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dosage, dailyUses, cost, instructions, patient, practitioner);
    }
}
