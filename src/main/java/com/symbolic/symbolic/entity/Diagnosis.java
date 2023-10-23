package com.symbolic.symbolic.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

/**
 * A data model for a diagnosis, representing the condition name, treatment information, and diagnosis date.
 */
@Entity
@Table(name = "diagnoses")
@NoArgsConstructor
public class Diagnosis {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "condition")
    private String condition;

    @Column(name = "treatmentInfo")
    private String treatmentInfo;

    @Column(name = "dateTime")
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date date;

    /**
     * A constructor for the Diagnosis data model.
     * @param condition a string value describing the condition that has been diagnosed
     * @param treatmentInfo a string value describing any details about the treatment plan for the diagnosis
     * @param date a Date value representing when the diagnosis was performed
     */
    public Diagnosis(String condition, String treatmentInfo, Date date) {
        this.condition = condition;
        this.treatmentInfo = treatmentInfo;
        this.date = date;
    }

    public Long getId() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Diagnosis diagnosis = (Diagnosis) o;
        return Objects.equals(id, diagnosis.id) && Objects.equals(condition, diagnosis.condition) && Objects.equals(treatmentInfo, diagnosis.treatmentInfo) && Objects.equals(date, diagnosis.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, condition, treatmentInfo, date);
    }
}
